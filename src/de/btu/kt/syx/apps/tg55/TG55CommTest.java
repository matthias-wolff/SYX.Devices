package de.btu.kt.syx.apps.tg55;

import java.util.concurrent.TimeoutException;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;

import org.junit.Test;
import org.junit.jupiter.api.DisplayName;

import de.btu.kt.syx.SYX;
import de.btu.kt.syx.midi.SyxDataStruct;
import de.btu.kt.syx.midi.SyxMessage;

public class TG55CommTest extends ATG55TestCase
{

  // -- Constants -------------------------------------------------------------

  // Test Names
  private static final String TEST_NAME
    = "TG55CommTest: Elementary SysEx Communication with a TG55 Instrument";

  // -- Tests -----------------------------------------------------------------

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

    final String  MI_OUT = getMidiInterfaceName();
    final String  MI_IN  = getMidiInterfaceName();
    MidiDevice    miOut  = null;
    MidiDevice    miIn   = null;
    MidiMessage[] mResp  = new MidiMessage[1];

    try
    {
      // Find and initialize MIDI devices
      log("Initialzing MIDI devices ...\n");
      for (MidiDevice.Info mdi : MidiSystem.getMidiDeviceInfo())
      {
        MidiDevice md = MidiSystem.getMidiDevice(mdi);
        if (mdi.getName().equals(MI_OUT) && md.getMaxReceivers()!=0)
        {
          log(String.format("- Opening MIDI-out device '%s' ...",MI_OUT));
          miOut = md;
          miOut.open();
          log(" ok\n");
        }
        if (mdi.getName().equals(MI_IN) && md.getMaxTransmitters()!=0)
        {
          log(String.format("- Opening MIDI-in device '%s' ...",MI_IN));
          miIn = md;
          miIn.open();
          log(" ok\n");
        }
      }
      if (miOut!=null && miIn!=null)
        log("ok\n\n");
      else if (miOut==null)
      {
        log(String.format("- MIDI-out device '%s' NOT FOUND\n",MI_OUT));
        throw new MidiUnavailableException();
      }
      else if (miIn==null)
      {
        log(String.format("- MIDI-in device '%s' NOT FOUND\n",MI_IN));
        throw new MidiUnavailableException();
      }
      
      // Listen to incoming MIDI messages 
      miIn.getTransmitter().setReceiver(new Receiver()
      {
        
        @Override
        public void send(MidiMessage message, long timeStamp)
        {
          log(0,3,"-> Received MIDI message:\n");
          mResp[0] = message;
          log(SYX.prettyPrintMidiMessage(mResp[0],timeStamp));
          log(0,-3,"\n");
        }
        
        @Override
        public void close()
        {
          // Nothing to be done
        }
      });
      
      // Request Multi bulk from TG55
      log("Requesting Multi bulk dump from TG55 ...\n");
      SyxDataStruct sRQ = new SyxDataStruct(F_TG55_BULK_REQUEST);
      sRQ.setName("Yamaha TG55 Multi Bulk Request");
      sRQ.setMidiValue('#',getDevNum()-1); // Device ID
      sRQ.setMidiValue('s',(byte)'M'    ); // Bulk type, 1st character
      sRQ.setMidiValue('t',(byte)'U'    ); // Bulk type, 2nd character
      sRQ.setMidiValue('x',0x7F         ); // Memory type - edit buffer
      sRQ.setMidiValue('y',0x00         ); // Memory number - (must be 0)
      SyxMessage mRQ = new SyxMessage(sRQ);
      log(0,3,"-> Send SysEx message to instrument:\n");
      log(mRQ.prettyPrint());
      miOut.getReceiver().send(mRQ,-1);
      log(0,-3,"\nok\n\n");
      
      // Wait for response from TG55
      log("Waiting for response from TG55 ... ");
      Thread.sleep(TIMEOUT);
      if (mResp[0]!=null)
        log("ok\n");
      else
      {
        log(String.format("TIMEOUT (%d ms)\n",TIMEOUT));
        throw new TimeoutException();
      }
    }
    finally
    {
      log("\n");
      if (miOut!=null)
      {
        log(String.format("Closing MIDI-out device '%s' ...",MI_OUT));
        miOut.close();
        log(" ok\n");
      }
      if (miIn!=null)
      {
        log(String.format("Closing MIDI-in device '%s' ...",MI_IN));
        miIn .close();
        log(" ok\n");
      }
    }
  }

}

// EOF