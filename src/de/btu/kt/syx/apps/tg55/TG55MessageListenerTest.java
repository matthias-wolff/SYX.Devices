package de.btu.kt.syx.apps.tg55;

import java.util.concurrent.TimeoutException;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.SysexMessage;

import org.junit.Test;
import org.junit.jupiter.api.DisplayName;

import de.btu.kt.syx.SYX;
import de.btu.kt.syx.midi.ISysexMessageListener;
import de.btu.kt.syx.midi.SyxChecksum;
import de.btu.kt.syx.midi.SyxDataStruct;
import de.btu.kt.syx.midi.SyxMessage;
import de.btu.kt.syx.util.MidiInterface;

/**
 * Test of the {@link ISysexMessageListener} interface and mechanism.
 * 
 * @see de.btu.kt.syx.apps.tg55
 * @see #test()
 */
public class TG55MessageListenerTest extends ATG55TestCase
{

  // Test Name
  private static final String TEST_NAME
    = "TG55SyesxMessageListenerTest: TG55 Multi Bulk Request";

  // MIDI Interface Settings
  private final int MI_VERBOSE = 1;

  // -- Tests -----------------------------------------------------------------

  /**
   * Tests the {@link ISysexMessageListener} interface and mechanism. Creates 
   * a SysEx message listener configured to receive TG55 bulk dumps, requests a 
   * Multi bulk dump from the synthesizer, waits for the response and displays 
   * the received bulk data.
   * 
   * @throws MidiUnavailableException
   * @throws InvalidMidiDataException
   * @throws InterruptedException
   * @throws TimeoutException
   */
  @Test
  @DisplayName(TEST_NAME)
  public void test()
  throws MidiUnavailableException, InvalidMidiDataException, 
         InterruptedException, TimeoutException 
  {
    System.out.println();
    setLogIndent(0);

    log("%s @%s\n",TEST_NAME,SYX.getHostName());
    logHrule();
    log("\n");

    MidiInterface mi = null;
    try
    {
      // Fetch synchronization semaphore
      class Semaphor
      {
        SysexMessage sxMsg = null;
        long timeStamp = -1;
      };

      // Create MIDI interface
      log(0,2,"Opening MIDI interface '%s' ... ",getMidiInterfaceName());
      mi = new MidiInterface(getMidiInterfaceName());
      mi.setVerbose(MI_VERBOSE);
      mi.open();
      log(0,-2,"ok\n\n");

      // Add a SysEx message listener to the MIDI interface
      String format = F_TG55_BD_HEADER1+" "+F_TG55_BD_HEADER2;
      SyxDataStruct sRF = new SyxDataStruct(format);
      sRF.setName("Yamaha TG55 Bulk Dump Header (response filter)");
      sRF.setMidiValue('#',getDevNum()-1); // Device ID
      Semaphor semaphor = new Semaphor();
      log("Listen to SysEx messages at '%s'\n",getMidiInterfaceName());
      log(0,2,"- Response filter:\n");
      log(sRF.prettyPrint());
      log(0,-2,"");
      mi.addSysExListener(new ISysexMessageListener()
      {
        
        @Override
        public void receiveSysexMsg(SysexMessage sxMsg, long timeStamp)
        throws InvalidMidiDataException
        {
          log("\nSysEx message received from instrument ");
          if (!sRF.match(sxMsg.getData(),sRF.getLength()))
          {
            log("REJECTED (does not match response filter)\n");
            throw new InvalidMidiDataException();
          }
          log("accepted\n");
          semaphor.sxMsg = sxMsg;
          semaphor.timeStamp = timeStamp;
          synchronized(semaphor)
          {
            semaphor.notifyAll();
          }
        }
        
        @Override
        public int getDevNum()
        {
          return TG55MessageListenerTest.this.getDevNum();
        }

      });
      log("\n");
      
      // Request Multi bulk from TG55
      log("Request Multi bulk dump from TG55\n");
      SyxDataStruct sRQ = new SyxDataStruct(F_TG55_BULK_REQUEST);
      sRQ.setName("Yamaha TG55 Multi Bulk Request");
      sRQ.setMidiValue('#',getDevNum()-1); // Device ID
      sRQ.setMidiValue('s',(byte)'M'); // Bulk type, 1st character
      sRQ.setMidiValue('t',(byte)'U'); // Bulk type, 2nd character
      sRQ.setMidiValue('x',0x7F     ); // Memory type - edit buffer
      sRQ.setMidiValue('y',0x00     ); // Memory number - (must be 0)
      SyxMessage mRQ = new SyxMessage(sRQ);
      log(0,3,"-> Send SysEx message to instrument:\n");
      log(mRQ.prettyPrint());
      long then = System.nanoTime();
      mi.send(mRQ);
      log(0,-3,"\n\n");

      // Wait for response
      log(0,2,"Waiting for response ... ");
      synchronized(semaphor)
      {
        semaphor.wait(TIMEOUT);
      }
      if (semaphor.sxMsg!=null)
      {
        // Received Multi dump from TG55
        long now =  System.nanoTime();;
        log(0,-2,"ok (%d ms)\n",(now-then)/1000000);
        SysexMessage msg = semaphor.sxMsg;
        log(0,3,"-> Received Multi bulk dump:");

        // Parse bulk data
        SyxMessage mBD = new SyxMessage();
        mBD.addParts(new SyxDataStruct(F_TG55_BD_HEADER1 ,"Bulk Header 1"));
        mBD.addParts(new SyxDataStruct(F_TG55_BD_HEADER2 ,"Bulk Header 2"));
        mBD.addParts(new SyxDataStruct(F_TG55_BDMU_HEADER,"Multi Common" ));
        mBD.addParts(new SyxDataStruct(F_TG55_BDMU_EFFECT,"Effect"       ));
        for (int i=0; i<16; i++)
        {
          String name = String.format("Channel %d Voice",i+1); 
          mBD.addParts(new SyxDataStruct(F_TG55_BDMU_VOICE,name));
        }
        mBD.addParts(new SyxChecksum(SyxChecksum.ROLAND,1,-1));
        mBD.setMessage(msg.getMessage(),mBD.getLength());
        log(mBD.prettyPrint(semaphor.timeStamp));
        log(0,-3,"\n\n");
      }
      else
      {
        log(0,-2,"TIMEOUT\n\n");
        throw new TimeoutException();
      }
    }
    finally
    {
      // Dispose MIDI interface
      if (mi!=null)
      {
        log(0,2,"Closing MIDI interface '%s' ... ",getMidiInterfaceName());
        mi.close();
        log(0,-2,"ok\n");
      }
      mi = null;
    }
    
  }
  
}

// EOF