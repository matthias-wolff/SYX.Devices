package de.btu.kt.syx.apps.vz1;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.SysexMessage;

import de.btu.kt.syx.apps.AAppc;
import de.btu.kt.syx.devices.vz1.VZ1;
import de.btu.kt.syx.devices.vz1.VZ1Patch;
import de.btu.kt.syx.midi.ISysexMessageListener;
import de.btu.kt.syx.midi.SyxDataStruct;
import de.btu.kt.syx.util.MidiInterface;

/**
 * Abstract test app for the VZ-1/VZ-10M device adapter.
 * 
 * @author Matthias Wolff
 */
public abstract class AVZ1Appc extends AAppc
{

  // -- Constants (Development) -----------------------------------------------

  private static final int VZ1_DEVNUM = 1;  // VZ device number

  //-- Nested Classes ---------------------------------------------------------

  /**
   * Dummy SysEx listener for log print-outs. VZ SysEx messages are processed
   * by {@link AVZ1Appc this}{@code .}{@link AVZ1Appc#vz1 vz1}{@code
   * .}{@link VZ1#processSysexMsg(SysexMessage) processSysexMsg}{@code (...)}.
   */
  abstract class ConsoleSysexListener implements ISysexMessageListener
  {
    private String logMsg;
  
    public ConsoleSysexListener(boolean before)
    {
      if (before)
        logMsg = "(MIDI input)";
      else
        logMsg = "> ";
    }
  
    @Override
    public void receiveSysexMsg(SysexMessage sxMsg, long timeStamp)
    throws InvalidMidiDataException
    {
      byte[] data = sxMsg.getData();
      SyxDataStruct msgHdr = new SyxDataStruct(VZ1.F_SYSEX_HEADER,data);
      if (msgHdr.getMidiValue('#')!=vz1.getDevNum()-1)
        throw new InvalidMidiDataException();
      log(logMsg); 
    }
  
    @Override
    public int getDevNum()
    {
      return 0; // Does not apply
    }
    
  }

  // -- Attributes ------------------------------------------------------------

  /**
   * The VZ-1/VZ-10M adapter instance.
   */
  protected static VZ1 vz1 = null;

  // -- Abstract Methods ------------------------------------------------------

  protected abstract int getVZ1Verbose();

  // -- Implementation of ATestAppc -------------------------------------------

  @Override
  protected void startup()
  {
    log(0,2,"Creating VZ-1 adapter instance ... ");
    vz1 = new VZ1(VZ1_DEVNUM, getVZ1Verbose());
    MidiInterface mi = getMidiInterface(); 
    if (mi!=null)
    {
      mi.addSysExListener(new ConsoleSysexListener(true){});
      vz1.setMidiIterface(mi);
      mi.addSysExListener(new ConsoleSysexListener(false){});
    }
    log(0,-2,"ok (%s)\n",vz1.getLogID());
    if (mi==null)
      errlog("VZ adapter running without MIDI interface\n");
  }

  @Override
  protected void shutdown()
  {
    // Do nothing
  }

  // -- Workers ---------------------------------------------------------------

  /**
   * Sends a patch to VZ.
   * 
   * <p>
   *   The method writes <b>{@code patch}</b> into the patch working buffer
   *   of the {@linkplain #vz1 VZ-1/VZ-10M adapter} (field {@link #vz1}) by
   *   invoking {@link #vz1}{@code .}{@link VZ1#setPatch(VZ1Patch)
   *   setToneData}{@code (patch)} before sending the patch.
   * </p>
   * 
   * @param patch
   *          The patch
   * @param target
   *          The target; see {@link VZ1#sendPatch(int)} for permissible
   *          midiValues
   */
  protected void setAndSendPatch(VZ1Patch patch, int target)
  {
    if (patch==null)
    {
      errlog("No tone data available\n");
      return;
    }
    try
    {
      vz1.setPatch(patch);
      log(0,2,"Sending patch '%s' to VZ ... ",patch.getName());
      vz1.sendPatch(target);
      log(0,-2,"ok\n");
    }
    catch (MidiUnavailableException e)
    {
      errlog(0,-2,"FAILED\n"); 
      log(0,-2,""); 
      errlog("Cause: %s (%s)\n",e.getMessage(),e.getClass().getSimpleName());
    }
  }

  /**
   * Sends a patch to VZ.
   * 
   * <p>
   *   Convenience shortcut for {@link #setAndSendPatch(VZ1Patch, int)
   *   setAndSendPatch}{@code (patch,}{@link VZ1#TM_NORMAL}{@code )}.
   * </p>
   * 
   * @param patch
   *          The patch
   */
  protected void setAndSendPatch(VZ1Patch patch)
  {
    setAndSendPatch(patch,VZ1.TM_NORMAL);
  }
  
}

// EOF