package de.btu.kt.syx.apps.vz1;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sound.midi.InvalidMidiDataException;

import de.btu.kt.syx.SYX;
import de.btu.kt.syx.devices.vz1.VZ1;
import de.btu.kt.syx.devices.vz1.VZ1Patch;
import de.btu.kt.syx.midi.SyxParamInfo;
import de.btu.kt.syx.util.SysexRecorder;

/**
 * Test app for {@link VZ1Patch} model. 
 * 
 * @author Matthias Wolff,
 *         <a href="https://www.b-tu.de/en/">BTU Cottbus-Senftenberg</a>
 */
public class VZ1PatchAppc extends AVZ1Appc
{
  // -- Constants (Development) -----------------------------------------------

  private static final int APP_VERBOSE = 1;  // Console app verbose level
  private static final int MI_VERBOSE  = 1;  // MIDI interface verbose level
  private static final int VZ1_VERBOSE = 2;  // VZ adapter verbose level

  private static final String VZ1_TD_BULKFN = "../tmp/VZ1_ToneData.txt";

  // -- Command Handlers -----------------------------------------------------

  /**
   * UI {@linkplain #getCommandHandlers() command handler} &ndash;
   * Shows or sets a parameter of the current {@linkplain VZ1#getPatch() VZ
   * patch buffer}. After setting the parameter, a Tone Data bulk dump is
   * sent to VZ.
   * 
   * @param command The UI command
   * @return {@code true}
   * @throws CommandNotAcceptedException
   *           if {@code command} does not match "&lt;{@code UID}&gt;" or
   *           "&lt;{@code UID}&gt;=&lt;{@code value}&gt" (see command help
   *           text)
   */
  public boolean onCmdGetSetParam(String command)
  throws CommandNotAcceptedException
  {
    // Print help
    if (command==null)
    {
      printHelpLine("*","Show all patch parameters");
      printHelpLine("*=","List permissible values for all patch parameters");
      printHelpLine("<UID>","Show patch parameter(s)");
      printHelpLine("<UID>=","List permissible values for patch parameter(s)");
      printHelpLine("<UID>=<val>","Set patch parameter(s)");
      printHelpLine("  <UID>: as listed by command '*', may contain wildcards:");
      printHelpLine("         *  matching any sequence of characters except '.'");
      printHelpLine("         ** matching any sequence of characters including '.'");
      printHelpLine("  <val>: as listed by command '<UID>='");
      return true;
    }
    
    // Parse command string
    Pattern pattern = Pattern.compile("([a-zA-Z_0-9.*]+)(\\s*=\\s*(\\S*))?");
    Matcher matcher = pattern.matcher(command);
    if (!matcher.matches())
      throw new CommandNotAcceptedException();
    //cmdResponse("Checking <UID>=<value>...");
    //for (int i=1; i<=matcher.groupCount(); i++)
    //  log("%d: '%s'\n",i,matcher.group(i));

    String  key = matcher.group(1);       // Parameter UID (pattern)
    boolean eqs = matcher.group(2)!=null; // Equals sign present
    String  val = matcher.group(3);       // Parameter value

    // Try to execute command string
    // - Get a patch
    VZ1Patch td = vz1.getPatch();
    if (td==null)
      td = new VZ1Patch();

    // - Get list of parameter UIDs to process
    String[] UIDs = td.findParamUIDsSimple(key);
    if (UIDs.length==0)
      throw new CommandNotAcceptedException();

    // Get maximal length of listed UIDs (for pretty alignment)
    int maxUIDlen = 0;
    for (String UID : UIDs)
      maxUIDlen = Math.max(maxUIDlen,UID.length());

    // - Process list of parameter UIDs
    boolean sendPatch = false;
    for (int i=0; i<UIDs.length; i++)
    {
      String UID = UIDs[i];
      if (!eqs)
        if (vz1.getPatch()!=null)
        { // Show parameter UID and value
          String v = vz1.getPatch().getModelValueAsString(UID);
          if (i==0)
            cmdResponse("show %d patch parameter%s",UIDs.length,UIDs.length>1?"s":"");
          log("  %-"+maxUIDlen+"s = %s\n",UID,v);
        }
        else
        { // Show parameter UID only
          if (i==0)
            cmdResponse("list matching patch parameter names");
          log("  %s\n",UID);
        }
      else if (val==null || val.isEmpty())
      { // List permissible values
        if (i==0)
          cmdResponse("list permissible patch parameter values");
        log("  %-"+maxUIDlen+"s in ",UID);
        SyxParamInfo pi = td.getParamInfo(UID);
        int pfxl = maxUIDlen+6;
        log(pi.prettyPrintModelValueSet(" ".repeat(pfxl),100-pfxl)+"\n");
      }
      else
      { // Set parameter value
        if (!checkToneDataBuf())
          return true;
        if (i==0)
          cmdResponse
          (
            "set %d patch parameter value%s",UIDs.length,UIDs.length>1?"s":""
          );
        try
        {
          vz1.getPatch().setModelValue(UID,val);
          String s = vz1.getPatch().getModelValueAsString(UID);
          log("  %-"+maxUIDlen+"s = %s\n",UID,s);
          sendPatch = true;
        }
        catch (InvalidMidiDataException e)
        {
          errlog("%s: value '%s' invalid!\n",UID,val);
        }
      }
    }
    // Send patch
    if (sendPatch)
      setAndSendPatch(vz1.getPatch());

    // Continue command loop
    return true;
  }

  /**
   * UI {@linkplain #getCommandHandlers() command handler} &ndash;
   * Dumps the current {@linkplain VZ1#getPatch() VZ patch buffer} at the
   * console.
   * 
   * @param command The UI command
   * @return {@code true}
   * @throws CommandNotAcceptedException
   *           if {@code command} is not "d" or "dd" (see command help text)
   */
  public boolean onCmdXDump(String command)
  throws CommandNotAcceptedException
  {
    // Print help
    if (command==null)
    {
      printHelpLine("d" ,"Dump Tone Data model to console");
      printHelpLine("dd","Dump Tone Data bulk message to console");
      return true;
    }
    
    // Check command string
    if (!command.equals("d") && !command.equals("dd"))
      throw new CommandNotAcceptedException();
    
    // Execute command
    if (!checkToneDataBuf())
      return true;

    if (command.equals("d"))
    {
      cmdResponse("dump Tone Data model");
      try
      {
        log(vz1.getPatch().prettyPrintModel());
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
    }
    else
    {
      cmdResponse("dump Tone Data bulk message");
      log(vz1.getPatch().prettyPrint()+"\n");
    }

    // Continue command loop
    return true;
  }

  /**
   * UI {@linkplain #getCommandHandlers() command handler} &ndash;
   * Writes the current {@linkplain VZ1#getPatch() VZ patch buffer} to a
   * temporary file.
   * 
   * @param command The UI command
   * @return {@code true}
   * @throws CommandNotAcceptedException if {@code command} is not "w"
   */
  public boolean onCmdXWrite(String command)
  throws CommandNotAcceptedException
  {
    // Print help
    if (command==null)
    {
      printHelpLine("w","Write tone data to temporary file");
      return true;
    }
    
    // Check command string
    if (!("w".equals(command)))
      throw new CommandNotAcceptedException();
    
    // Execute command
    if (!checkToneDataBuf())
      return true;

    String fname = SYX.getPath(VZ1PatchAppc.class,VZ1_TD_BULKFN,SYX.SOURCE|SYX.ROOT);
    String resp  = String.format("writing tone data to %s ...",fname);
    SysexRecorder recorder = new SysexRecorder();
    recorder.record(vz1.getPatch());
    try
    {
      recorder.writeTxtFile(fname);
      cmdResponse(resp+" ok");
    }
    catch (Exception e)
    {
      cmdResponse(resp+" FAILED");
      log(e);
    }
    
    // Continue command loop
    return true;
  }

  /**
   * UI {@linkplain #getCommandHandlers() command handler} &ndash; 
   * Reads the current {@linkplain VZ1#getPatch() VZ patch buffer} from a
   * temporary file.
   * 
   * @param command The UI command
   * @return {@code true}
   * @throws CommandNotAcceptedException if {@code command} is not "r"
   */
  public boolean onCmdXRead(String command)
  throws CommandNotAcceptedException
  {
    // Print help
    if (command==null)
    {
      printHelpLine("r","Read tone data from temporary file");
      return true;
    }
    
    // Check command string
    if (!("r".equals(command)))
      throw new CommandNotAcceptedException();

    // Execute command
    String fname = SYX.getPath(VZ1PatchAppc.class,VZ1_TD_BULKFN,SYX.SOURCE|SYX.ROOT);
    String resp  = String.format("reading tone data from %s ...",fname); 
    SysexRecorder recorder = new SysexRecorder();
    try
    {
      recorder.readTxtFile(fname);
      vz1.setPatch(new VZ1Patch(recorder.getTape()[0].getData()));
      cmdResponse(resp+" ok");
    }
    catch (Exception e)
    {
      cmdResponse(resp+" FAILED");
      log(e);
    }
    
    // Continue command loop
    return true;
  }

  /**
   * UI {@linkplain #getCommandHandlers() command handler} &ndash; 
   * Creates a demo patch.
   * 
   * @param command The UI command
   * @return {@code true}
   * @throws CommandNotAcceptedException 
   *           if {@code command} does not start with "p" or the remaining 
   *           command string is malformed (see command help text)
   */
  public boolean onCmdXPatch(String command)
  throws CommandNotAcceptedException
  {
    // Print help
    if (command==null)
    {
      printHelpLine("p<n>","Create demo patch; <n> in {0,1}");
      return true;
    }

    // Check command string
    Pattern pattern = Pattern.compile("p(\\d+)");
    Matcher matcher = pattern.matcher(command);
    if (!matcher.matches())
      throw new CommandNotAcceptedException();

    // Execute command
    int n = Integer.parseInt(matcher.group(1));
    cmdResponse(String.format("create demo patch #%d",n));
    VZ1Patch td = null;
    switch (n)
    {
    case 0:
      td = VZ1PatchAppc.vz1Patch_NothingToFear();
      break;
    case 1:
      td = VZ1PatchAppc.vz1Patch_Karamoon();
      break;
    default:
      errlog("Invalid patch number (must be in {0,1})\n");
      return true;
    }
    setAndSendPatch(td,VZ1.TM_NORMAL);

    // Continue command loop
    return true;
  }
  
  /**
   * UI {@linkplain #getCommandHandlers() command handler} &ndash;
   * Debugging command.
   * 
   * @param command The UI command
   * @return {@code true}
   * @throws CommandNotAcceptedException if {@code command} is not "x"
   */
  public boolean onCmdYDebug(String command)
  throws CommandNotAcceptedException
  {
    // Print help
    if (command==null)
    {
      printHelpLine("x","Debugging command");
      return true;
    }
    
    // Check command string
    if (!("x".equals(command)))
      throw new CommandNotAcceptedException();

    // Execute command
    cmdResponse("debug");

    // Continue command loop
    return true;
  }

  // -- Jonas' Commmand Handlers ----------------------------------------------
  
  /**
   * UI {@linkplain #getCommandHandlers() command handler} &ndash; 
   * Initializes thethe current {@linkplain VZ1#getPatch() VZ patch buffer} with
   * default midiValues.
   * 
   * @param command The UI command
   * @return {@code true}
   * @throws CommandNotAcceptedException if {@code command} is not "i"
   */
  public boolean onCmdXInitialize(String command)
  throws CommandNotAcceptedException
  {
    // Print help
    if (command==null)
    {
      printHelpLine("i","Initialize tone data with default midiValues");
      return true;
    }
    
    // Check command string
    if (!("i".equals(command)))
      throw new CommandNotAcceptedException();

    // Execute command
    cmdResponse("initialize");
    
    // log("Hello world!\n"); // Print to console, see String.format
    // err.log("Error\n"); // Print error to console
    //if (!checkToneDataBuf())
    //  return true;
    
    try
    {
      VZ1Patch td = vz1.getPatch();
      if (td!=null)
        td.reset();
      else
        td = new VZ1Patch(); 
      log(td.prettyPrintModel());
      setAndSendPatch(td);
    }
    catch (Throwable e)
    {
      errlog(e);
    }

    // Continue command loop
    return true;
  }

  // -- Implementation of ATestAppc -------------------------------------------

  @Override
  public String getLogID()
  {
    return "VZ1 PTCH";
  }

  @Override
  protected String getAppName()
  {
    return "SYX -- VZ-1/VZ-10M SINGLE PATCH TEST APP";
  }

  @Override
  public int getVerbose()
  {
    return APP_VERBOSE;
  }

  @Override
  protected int getMidiVerbose()
  {
    return MI_VERBOSE;
  }

  @Override
  protected int getVZ1Verbose()
  {
    return VZ1_VERBOSE;
  }

  @Override
  protected int getHelpPrefixLength()
  {
    return 11;
  }

  @Override
  protected String getExtraHelpText()
  {
    return
     "How to Trigger VZ SysEx Bulk Dumps\n"
     + "- Enable system exclusive messages: Menu 3-04, parameter EXCLUSIVE: ENA *\n"
     + "- Tone data: Press [NORMAL], then select patch or press [COMPARE/RECALL]\n\n"
     + "* Setting not persistent! Repeat every time after power-up!";

  }

  @Override
  protected void startup()
  {
    super.startup();
  }

  @Override
  protected void shutdown()
  {
    super.shutdown();
  }

  // -- Auxiliary Methods -----------------------------------------------------

  /**
   * Returns {@code true} if there is data in the {@linkplain #toneDataBuf VZ 
   * tone data buffer}, otherwise prints an error message and returns {@code 
   * false}.
   */
  private boolean checkToneDataBuf()
  {
    if (vz1.getPatch()!=null)
      return true;

    errlog
    (
      "Patch buffer empty (send tone data bulk from VZ or load tone data "
      + "file!)\n"
    );
    return false;
  }

  /**
   * Returns the demo patch "{@code NOTH.TO FEAR}".
   */
  public static VZ1Patch vz1Patch_NothingToFear()
  {
    VZ1Patch td = new VZ1Patch();
    try
    {
      // Global Settings
      td.setName("NOTH.TO FEAR");
      td.setTotalLevel(99);
      td.setOctave(-1);
      td.setTremolo(false,VZ1.TRIANG,00,00,00);
      td.setVibrato(false,VZ1.TRIANG,04,01,00);
  
      // Algorithm
      td.setLineAlgoritm(VZ1.A,VZ1.MIX  );
      td.setLineAlgoritm(VZ1.B,VZ1.PHASE); td.setExtPhase(VZ1.B,false);
      td.setLineAlgoritm(VZ1.C,VZ1.MIX  ); td.setExtPhase(VZ1.C,false);
      td.setLineAlgoritm(VZ1.D,VZ1.PHASE); td.setExtPhase(VZ1.D,false);
  
      // Module Settings
      // - M1
      td.setModuleOn         (VZ1.M1,true        );
      td.setWaveform         (VZ1.M1,VZ1.SAW5    );
      td.setDetuneRel        (VZ1.M1,-1,00,00,06 );
      td.setAmpEnvStep       (VZ1.M1,VZ1.S1,51,99);
      td.setAmpEnvStep       (VZ1.M1,VZ1.S2,28,00);
      td.setAmpEnvSustainStep(VZ1.M1,VZ1.S1      );
      td.setAmpEnvEndStep    (VZ1.M1,VZ1.S2      );
      td.setAmpEnvDepth      (VZ1.M1,99          );
      td.setAmpVelSens       (VZ1.M1,VZ1.C1,00   );
      td.setAmpEffSens       (VZ1.M1,00          );

      // - M2
      td.setModuleOn         (VZ1.M2,true        );
      td.setWaveform         (VZ1.M2,VZ1.SAW2    );
      td.setDetuneRel        (VZ1.M2,+1,00,00,03 );
      td.setAmpEnvStep       (VZ1.M2,VZ1.S1,57,99);
      td.setAmpEnvStep       (VZ1.M2,VZ1.S2,28,00);
      td.setAmpEnvSustainStep(VZ1.M2,VZ1.S1      );
      td.setAmpEnvEndStep    (VZ1.M2,VZ1.S2      );
      td.setAmpEnvDepth      (VZ1.M2,99          );
      td.setAmpVelSens       (VZ1.M2,VZ1.C1,00   );
      td.setAmpEffSens       (VZ1.M2,00          );
  
      // - M3
      td.setModuleOn         (VZ1.M3,true        );
      td.setWaveform         (VZ1.M3,VZ1.SAW2    );
      td.setDetuneRel        (VZ1.M3,+1,00,00,04 );
      td.setAmpEnvStep       (VZ1.M3,VZ1.S1,57,99);
      td.setAmpEnvStep       (VZ1.M3,VZ1.S2,19,00);
      td.setAmpEnvSustainStep(VZ1.M3,VZ1.S1      );
      td.setAmpEnvEndStep    (VZ1.M3,VZ1.S2      );
      td.setAmpEnvDepth      (VZ1.M3,84          );
      td.setAmpVelSens       (VZ1.M3,VZ1.C1,00   );
      td.setAmpEffSens       (VZ1.M3,00          );
  
      // - M4
      td.setModuleOn         (VZ1.M4,true        );
      td.setWaveform         (VZ1.M4,VZ1.SAW1    );
      td.setDetuneRel        (VZ1.M4,-1,00,00,04 );
      td.setAmpEnvStep       (VZ1.M4,VZ1.S1,57,99);
      td.setAmpEnvStep       (VZ1.M4,VZ1.S2,27,00);
      td.setAmpEnvSustainStep(VZ1.M4,VZ1.S1      );
      td.setAmpEnvEndStep    (VZ1.M4,VZ1.S2      );
      td.setAmpEnvDepth      (VZ1.M4,92          );
      td.setAmpVelSens       (VZ1.M4,VZ1.C1,00   );
      td.setAmpEffSens       (VZ1.M4,00          );
  
      // - M5
      td.setModuleOn         (VZ1.M5,true        );
      td.setWaveform         (VZ1.M5,VZ1.SAW5    );
      td.setDetuneRel        (VZ1.M5,+1,00,00,12 );
      td.setAmpEnvStep       (VZ1.M5,VZ1.S1,57,99);
      td.setAmpEnvStep       (VZ1.M5,VZ1.S2,28,00);
      td.setAmpEnvSustainStep(VZ1.M5,VZ1.S1      );
      td.setAmpEnvEndStep    (VZ1.M5,VZ1.S2      );
      td.setAmpEnvDepth      (VZ1.M5,99          );
      td.setAmpVelSens       (VZ1.M5,VZ1.C1,00   );
      td.setAmpEffSens       (VZ1.M5,00          );
  
      // - M6
      td.setModuleOn         (VZ1.M6,true        );
      td.setWaveform         (VZ1.M6,VZ1.SAW5    );
      td.setDetuneRel        (VZ1.M6,+1,00,00,00 );
      td.setAmpEnvStep       (VZ1.M6,VZ1.S1,57,99);
      td.setAmpEnvStep       (VZ1.M6,VZ1.S2,28,00);
      td.setAmpEnvSustainStep(VZ1.M6,VZ1.S1      );
      td.setAmpEnvEndStep    (VZ1.M6,VZ1.S2      );
      td.setAmpEnvDepth      (VZ1.M6,99          );
      td.setAmpVelSens       (VZ1.M6,VZ1.C1,00   );
      td.setAmpEffSens       (VZ1.M6,00          );
  
      // - M7
      td.setModuleOn         (VZ1.M7,true        );
      td.setWaveform         (VZ1.M7,VZ1.SAW1    );
      td.setDetuneRel        (VZ1.M7,+1,00,00,18 );
      td.setAmpEnvStep       (VZ1.M7,VZ1.S1,57,99);
      td.setAmpEnvStep       (VZ1.M7,VZ1.S2,19,00);
      td.setAmpEnvSustainStep(VZ1.M7,VZ1.S1      );
      td.setAmpEnvEndStep    (VZ1.M7,VZ1.S2      );
      td.setAmpEnvDepth      (VZ1.M7,75          );
      td.setAmpVelSens       (VZ1.M7,VZ1.C1,00   );
      td.setAmpEffSens       (VZ1.M7,00          );
  
      // - M8
      td.setModuleOn         (VZ1.M8,true        );
      td.setWaveform         (VZ1.M8,VZ1.SAW2    );
      td.setDetuneRel        (VZ1.M8,+1,00,00,00 );
      td.setAmpEnvStep       (VZ1.M8,VZ1.S1,57,99);
      td.setAmpEnvStep       (VZ1.M8,VZ1.S2,24,00);
      td.setAmpEnvSustainStep(VZ1.M8,VZ1.S1      );
      td.setAmpEnvEndStep    (VZ1.M8,VZ1.S2      );
      td.setAmpEnvDepth      (VZ1.M8,92          );
      td.setAmpVelSens       (VZ1.M8,VZ1.C1,00   );
      td.setAmpEffSens       (VZ1.M8,00          );
  
      // Amplitude Key Following: Use defaults (no key following)
  
      // Pitch Envelope Settings
      td.setPitchEnvDepth      (63          );
      td.setPitchEnvRange      (VZ1.NARROW  );
      td.setPitchEnvStep       (VZ1.S1,99,00);
      td.setPitchEnvSustainStep(VZ1.S1      );
      td.setPitchEnvEndStep    (VZ1.S1      );
      td.setPitchVelSens       (VZ1.C1,00   );
  
      // Pitch Key Following: Use defaults (no key following)
      // Key Following Rate : Use defaults (no key following)
  
      return td;
    }
    catch (Throwable e)
    { // Cannot happen
      throw SYX.InternalError(e);
    }
  }

  /**
   * Returns the demo patch "{@code KARAMOON}".
   */
  public static VZ1Patch vz1Patch_Karamoon()
  {
    VZ1Patch td = new VZ1Patch();
    try
    {
      // Global Settings
      td.setName("KARAMOON");
      td.setTotalLevel(99);
      td.setOctave(+2);
      td.setTremolo(false,VZ1.TRIANG,00,63,00);
      td.setVibrato(false,VZ1.TRIANG,00,00,00);
  
      // Algorithm
      td.setLineAlgoritm(VZ1.A,VZ1.RING );
      td.setLineAlgoritm(VZ1.B,VZ1.PHASE); td.setExtPhase(VZ1.B,false);
      td.setLineAlgoritm(VZ1.C,VZ1.MIX  ); td.setExtPhase(VZ1.C,false);
      td.setLineAlgoritm(VZ1.D,VZ1.MIX  ); td.setExtPhase(VZ1.D,false);
  
      // Module Settings
      // - M1
      td.setModuleOn         (VZ1.M1,true        );
      td.setWaveform         (VZ1.M1,VZ1.SAW5    );
      td.setDetuneRel        (VZ1.M1,-3,00,00,01 );
      td.setAmpEnvStep       (VZ1.M1,VZ1.S1,99,99);
      td.setAmpEnvStep       (VZ1.M1,VZ1.S2,31,49);
      td.setAmpEnvStep       (VZ1.M1,VZ1.S3,31,00);
      td.setAmpEnvSustainStep(VZ1.M1,VZ1.S2      );
      td.setAmpEnvEndStep    (VZ1.M1,VZ1.S3      );
      td.setAmpEnvDepth      (VZ1.M1,99          );
      td.setAmpVelSens       (VZ1.M1,VZ1.C2,31   );
      td.setAmpEffSens       (VZ1.M1,00          );
  
      // - M2
      td.setModuleOn         (VZ1.M2,true        );
      td.setWaveform         (VZ1.M2,VZ1.SINE    );
      td.setDetuneRel        (VZ1.M2,-1,01,00,00 );
      td.setAmpEnvStep       (VZ1.M2,VZ1.S1,99,99);
      td.setAmpEnvStep       (VZ1.M2,VZ1.S2,43,00);
      td.setAmpEnvSustainStep(VZ1.M2,VZ1.S1      );
      td.setAmpEnvEndStep    (VZ1.M2,VZ1.S2      );
      td.setAmpEnvDepth      (VZ1.M2,99          );
      td.setAmpVelSens       (VZ1.M2,VZ1.C2,31   );
      td.setAmpEffSens       (VZ1.M2,00          );
  
      // - M3
      td.setModuleOn         (VZ1.M3,true        );
      td.setWaveform         (VZ1.M3,VZ1.SAW1    );
      td.setDetuneRel        (VZ1.M3,-4,00,00,00 );
      td.setAmpEnvStep       (VZ1.M3,VZ1.S1,96,65);
      td.setAmpEnvStep       (VZ1.M3,VZ1.S2,30,99);
      td.setAmpEnvSustainStep(VZ1.M3,VZ1.S2      );
      td.setAmpEnvEndStep    (VZ1.M3,VZ1.S2      );
      td.setAmpEnvDepth      (VZ1.M3,40          );
      td.setAmpVelSens       (VZ1.M3,VZ1.C1,04   );
      td.setAmpEffSens       (VZ1.M3,00          );
  
      // - M4
      td.setModuleOn         (VZ1.M4,true        );
      td.setWaveform         (VZ1.M4,VZ1.SINE    );
      td.setDetuneRel        (VZ1.M4,+0,00,00,00 );
      td.setAmpEnvStep       (VZ1.M4,VZ1.S1,99,99);
      td.setAmpEnvStep       (VZ1.M4,VZ1.S2,68,99);
      td.setAmpEnvStep       (VZ1.M4,VZ1.S3,27,00);
      td.setAmpEnvSustainStep(VZ1.M4,VZ1.S1      );
      td.setAmpEnvEndStep    (VZ1.M4,VZ1.S3      );
      td.setAmpEnvDepth      (VZ1.M4,69          );
      td.setAmpVelSens       (VZ1.M4,VZ1.C1,04   );
      td.setAmpEffSens       (VZ1.M4,00          );
  
      // - M5
      td.setModuleOn         (VZ1.M5,true        );
      td.setWaveform         (VZ1.M5,VZ1.SAW3    );
      td.setDetuneRel        (VZ1.M5,-1,02,11,54 );
      td.setAmpEnvStep       (VZ1.M5,VZ1.S1,46,65);
      td.setAmpEnvStep       (VZ1.M5,VZ1.S2,30,99);
      td.setAmpEnvStep       (VZ1.M5,VZ1.S3,26,00);
      td.setAmpEnvSustainStep(VZ1.M5,VZ1.S2      );
      td.setAmpEnvEndStep    (VZ1.M5,VZ1.S3      );
      td.setAmpEnvDepth      (VZ1.M5,68          );
      td.setAmpVelSens       (VZ1.M5,VZ1.C1,03   );
      td.setAmpEffSens       (VZ1.M5,00          );
  
      // - M6
      td.setModuleOn         (VZ1.M6,true        );
      td.setWaveform         (VZ1.M6,VZ1.SAW3    );
      td.setDetuneRel        (VZ1.M6,-1,03,00,07 );
      td.setAmpEnvStep       (VZ1.M6,VZ1.S1,99,99);
      td.setAmpEnvStep       (VZ1.M6,VZ1.S2,68,99);
      td.setAmpEnvStep       (VZ1.M6,VZ1.S3,37,00);
      td.setAmpEnvSustainStep(VZ1.M6,VZ1.S1      );
      td.setAmpEnvEndStep    (VZ1.M6,VZ1.S3      );
      td.setAmpEnvDepth      (VZ1.M6,68          );
      td.setAmpVelSens       (VZ1.M6,VZ1.C1,04   );
      td.setAmpEffSens       (VZ1.M6,00          );
  
      // - M7
      td.setModuleOn         (VZ1.M7,true        );
      td.setWaveform         (VZ1.M7,VZ1.SAW5    );
      td.setDetuneRel        (VZ1.M7,-1,03,00,04 );
      td.setAmpEnvStep       (VZ1.M7,VZ1.S1,94,97);
      td.setAmpEnvStep       (VZ1.M7,VZ1.S2,59,62);
      td.setAmpEnvStep       (VZ1.M7,VZ1.S3,30,00);
      td.setAmpEnvSustainStep(VZ1.M7,VZ1.S3      );
      td.setAmpEnvEndStep    (VZ1.M7,VZ1.S3      );
      td.setAmpEnvDepth      (VZ1.M7,50          );
      td.setAmpVelSens       (VZ1.M7,VZ1.C1,01   );
      td.setAmpEffSens       (VZ1.M7,00          );
  
      // - M8
      td.setModuleOn         (VZ1.M8,true        );
      td.setWaveform         (VZ1.M8,VZ1.SAW5    );
      td.setDetuneRel        (VZ1.M8,-1,02,11,58 );
      td.setAmpEnvStep       (VZ1.M8,VZ1.S1,99,90);
      td.setAmpEnvStep       (VZ1.M8,VZ1.S2,50,50);
      td.setAmpEnvStep       (VZ1.M8,VZ1.S3,25,50);
      td.setAmpEnvSustainStep(VZ1.M8,VZ1.S1      );
      td.setAmpEnvEndStep    (VZ1.M8,VZ1.S3      );
      td.setAmpEnvDepth      (VZ1.M8,50          );
      td.setAmpVelSens       (VZ1.M8,VZ1.C1,04   );
      td.setAmpEffSens       (VZ1.M8,00          );
  
      // Amplitude Key Following: Use defaults (no key following)
  
      // Pitch Envelope Settings
      td.setPitchEnvDepth      (63          );
      td.setPitchEnvRange      (VZ1.NARROW  );
      td.setPitchEnvStep       (VZ1.S1,99,00);
      td.setPitchEnvSustainStep(VZ1.S1      );
      td.setPitchEnvEndStep    (VZ1.S1      );
      td.setPitchVelSens       (VZ1.C1,01   );
  ;
      // Pitch Key Following: Use defaults (no key following)
      // Key Following Rate : Use defaults (no key following)
  
      return td;
    }
    catch (Throwable e)
    { // Cannot happen
      throw SYX.InternalError(e);
    }
  }

  // -- MAIN ------------------------------------------------------------------

  public static void main(String[] args) 
  {
    VZ1PatchAppc theApp = new VZ1PatchAppc();
    theApp.run(true);
  }

}

// EOF