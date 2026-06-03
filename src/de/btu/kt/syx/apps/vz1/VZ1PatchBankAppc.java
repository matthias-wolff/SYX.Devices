package de.btu.kt.syx.apps.vz1;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.sound.midi.InvalidMidiDataException;

import de.btu.kt.syx.SYX;
import de.btu.kt.syx.devices.BankTree;
import de.btu.kt.syx.devices.vz1.VZ1;
import de.btu.kt.syx.devices.vz1.VZ1Patch;
import de.btu.kt.syx.midi.SyxDataStruct;
import de.btu.kt.syx.midi.SyxMessage;
import de.btu.kt.syx.util.SysexRecorder;

/**
 * Test app for VZ Tone/Operation SysEx bulks and {@link BankTree}s. 
 * 
 * 
 * @author Matthias Wolff,
 *         <a href="https://www.b-tu.de/en/">BTU Cottbus-Senftenberg</a>
 *
 * <p><b style="margin-left:-3.3em">Mood:</b><br>
 *   <a href="https://de.wikipedia.org/wiki/AG._Geige">AG. Geige</a>, "Yachtclub
 *   + Buchteln", "Trickbeat", "Raabe?", "3"
 * </p>
 */
public class VZ1PatchBankAppc extends AVZ1Appc
{

  // -- Constants -------------------------------------------------------------

  private static final int MI_VERBOSE  = 1;  // MIDI verbose level
  private static final int VZ1_VERBOSE = 2;  // VZ adapter verbose level

  private static String VZ1_DATA_TAPE_HEADER = "44H 03H 00H 7#H 7tH";
  private static String VZ1_TD_TRACK = "0*H[672] 0*******";
  private static String VZ1_OP_TRACK = "0*H[200] 0*******";

  private static final String E_PATCHBANK_EMPTY 
    = "Not possible (patch bank #%02d is empty)";

  // -- Attributes ------------------------------------------------------------

  /**
   * The VZ patch bank.
   */
  BankTree<VZ1Patch> patchBank = null;

  /**
   * The directory containing bank dump files.
   */
  String bankDir = null;

  /**
   * The index of the currently active patch bank.
   */
  int activePatchBank = 0;

  /**
   * Index of last sent patch (-1 of none)
   */
  int lastPatchIndex = -1;

  // -- Command Handlers ------------------------------------------------------

  /**
   * UI {@linkplain #getCommandHandlers() command handler} &ndash; 
   * Prints the current VZ patch bank.
   * 
   * @param command The UI command
   * @return {@code true}
   * @throws CommandNotAcceptedException
   *   if {@code command} is not "d" or "dd" (see command help text)
   */
  public boolean onCmdDPrintActivePatchBank(String command)
  throws CommandNotAcceptedException
  {
    // Print help
    if (command==null)
    {
      printHelpLine("d" ,"Print VZ patch bank");
      printHelpLine("dd","Print patch tree");
      return true;
    }
    
    // Execute command
    switch (command)
    {
    case "d":
      cmdResponse
      (
        String.format("print active patch bank #%02d",this.activePatchBank+1)
      );
      prettyPrintPatchBank();
      return true; // Continue command loop
    case "dd":
      cmdResponse("show patch tree");
      if (this.patchBank!=null)
        log(this.patchBank.prettyPrint());
      return true; // Continue command loop
    default:
      // Command not accepted
      throw new CommandNotAcceptedException();
    }
  }

  /**
   * UI {@linkplain #getCommandHandlers() command handler} &ndash; 
   * Reads a VZ patch bank for a Sysex file.
   * 
   * @param command The UI command
   * @return {@code true}
   * @throws CommandNotAcceptedException 
   *           if {@code command} does not start with "p" or the remaining 
   *           command string is malformed (see command help text)
   */
  public boolean onCmdPActivatePatchBank(String command)
  throws CommandNotAcceptedException
  {
    // Print help
    if (command==null)
    {
      printHelpLine("p"   ,"Print available patch banks");
      printHelpLine("p*"  ,"Load all patch banks");
      printHelpLine("p<n>","Activate patch bank <n>");
      return true;
    }
    
    // Pre-check command string
    if (!command.startsWith("p"))
      throw new CommandNotAcceptedException();

    // Execute command
    Pattern pattern = Pattern.compile("p(\\d*)");
    Matcher matcher = pattern.matcher(command);
    if (matcher.matches())
    {
      try
      {
        if (matcher.group(1).length()==0)
        {
          cmdResponse("patch bank overview (* loaded, A active)");
          printAvailablePatchBanks();
        }
        else
        {
          int bIndex = Integer.parseInt(matcher.group(1))-1;
          cmdResponse(String.format("activate patch bank #%02d",(bIndex+1)));
          if (bIndex<0 || bIndex>=getPatchBankCount())
          {
            errlog("Patch bank %d dos not exist (see command 'p')\n",bIndex+1);
            return true; // Continue command loop
          }
          ErrorReport er = new ErrorReport();
          BankTree<VZ1Patch>.Bank bank = getPatchBank(bIndex);
          if (bank.getBankObjectUIDs().size()==0)
            er.add(loadPatchBank(bank));
          if (er.nErrors>0)
          {
            errlog
            (
              "There were parameter validation errors in %d patch%s\n",
              er.nErrors,er.nErrors>0?"es":""
            );
            if (getVerbose()<2)
              errlog("(For details, reset and re-run with verbose level 2)\n");
          }
          this.activePatchBank = bIndex;
          this.lastPatchIndex  = -1;
          log(0,0,"Patch banks (* loaded, A active):\n");
          printAvailablePatchBanks();
        }
      }
      catch (Exception e)
      {
        log(e);
      }
      return true; // Continue command loop
    }
    else if ("p*".equals(command))
    {
      cmdResponse("load all patch banks");
      ErrorReport er = new ErrorReport();
      for (BankTree<VZ1Patch>.Bank bank : this.patchBank.getBanks(true))
      {
        if (bank.getBankObjectUIDs().size()==0)
          er.add(loadPatchBank(bank));
        er.add(loadPatchBank(bank));
      }
      if (er.nErrors>0)
        errlog
        (
          "There were parameter validation errors in %d patch%s\n",
          er.nErrors,er.nErrors>0?"es":""
        );
      if (getVerbose()<2)
        errlog("(For details, reset and re-run with verbose level 2)\n");
      log(0,0,"Patch banks (* loaded, A active):\n");
      printAvailablePatchBanks();
      return true; // Continue command loop
    }
    
    // Command not accepted
    throw new CommandNotAcceptedException();
  }

  /**
   * UI {@linkplain #getCommandHandlers() command handler} &ndash; 
   * Sends a patch to VZ.
   * 
   * @param command 
   *          The UI command, a VZ patch identifier matching {@code 
   *          [a-hA-H]-?[1-8]}
   * @return {@code true}
   * @throws CommandNotAcceptedException 
   *           if {@code command} does no match the pattern above
   */
  public boolean onCmdSendPatch(String command)
  throws CommandNotAcceptedException
  {
    // Print help
    if (command==null)
    {
      printHelpLine
      (
        "<s><n>",
        "Send patch to VZ (<s> in [a-h,A-H] and <n> in [1-8])"
      );
      return true;
    }
    
    // Check command string
    Pattern pattern = Pattern.compile("([a-hA-H])-?([1-8])");
    Matcher matcher = pattern.matcher(command);
    if (!matcher.matches())
      throw  new CommandNotAcceptedException();

    // Execute command
    int pIndex = VZ1.bankElemId2Index(command);
    this.lastPatchIndex = pIndex;
    cmdResponse(String.format("send patch %s",VZ1.bankElemIndex2Id(pIndex)));
    if (getActivePatchBank().getBankObjects().size()==0)
    {
      errlog(E_PATCHBANK_EMPTY+"\n",this.activePatchBank+1);
      return true; // Continue command loop
    }
    VZ1Patch patch = getActivePatchBank().getBankObjects().get(pIndex).getObject();
    setAndSendPatch(patch);

    // Continue command loop
    return true; 
  }

  /**
   * UI {@linkplain #getCommandHandlers() command handler} &ndash; 
   * Sends the next patch from {@link #_old_patchBank} to VZ.
   * 
   * @param command The UI command
   * @return {@code true}
   * @throws CommandNotAcceptedException 
   *           if {@code command} is not "" (see command help text)
   */
  public boolean onCmdSendPatchNext(String cmd)
  throws CommandNotAcceptedException
  {
    // Print help
    if (cmd==null)
    {
      printHelpLine("[empty]","Send next patch to VZ");
      return true;
    }
    
    // Check command string
    if (cmd.length()>0)
      throw new CommandNotAcceptedException();

    // Execute command
    try
    {
      int pIndex = (this.lastPatchIndex+1)%64;
      onCmdSendPatch(VZ1.bankElemIndex2Id(pIndex));
    }
    catch (Exception e)
    {
      log(e);
    }
    
    // Continue command loop
    return true;
  }

  /**
   * UI {@linkplain #getCommandHandlers() command handler} &ndash;
   * Resets the patch bank
   *
   * @param command The UI command
   * @return {@code true}
   * @throws CommandNotAcceptedException 
   *           if {@code command} is not "r"
   */
  public boolean onCmdReset(String cmd)
  throws CommandNotAcceptedException
  {
    // Print help
    if (cmd==null)
    {
      printHelpLine("r","Reset");
      return true;
    }
    
    // Check command string
    if (!"r".equals(cmd))
      throw new CommandNotAcceptedException();
    
    // Execute command
    cmdResponse("reset");
    reset();
    log(0,0,"Patch banks (* loaded, A active):\n");
    printAvailablePatchBanks();

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
  public boolean onCmdXDebug(String cmd)
  throws CommandNotAcceptedException
  {
    // Print help
    if (cmd==null)
    {
      printHelpLine("x","Debug command");
      return true;
    }
    
    // Check command string
    if (!"x".equals(cmd))
      throw new CommandNotAcceptedException();

    // Execute Command
    cmdResponse("debug");
    try
    {
    }
    catch (Exception e)
    {
      log(e);
    }

    // Continue command loop
    return true;
  }

  // -- Implementation of ATestAppc -------------------------------------------
  
  @Override
  public String getLogID()
  {
    return "VZ1 xBNK";
  }

  @Override
  protected String getAppName() 
  {
    return "SYX -- VZ-1/VZ-10M PATCH BANK TEST APP";
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
    return 7;
  }

  @Override
  protected String getExtraHelpText()
  {
    return
      "System exclusive messages must be enabled every time after VZ power-up!\n"
      + "Menu 3-04, parameter EXCLUSIVE: ENA. The setting is not persistent.";
  }

  @Override
  protected void startup()
  {
    super.startup();
    reset();
  }

  @Override
  protected void shutdown()
  {
    super.shutdown();
  }

  // -- Workers ---------------------------------------------------------------

  /**
   * Resets the app.
   */
  protected void reset()
  {
    this.patchBank       = new BankTree<VZ1Patch>("Patch","VZ Patch Bank");
    this.bankDir         = null;
    this.activePatchBank = 0;
    this.lastPatchIndex  = -1;
    try
    {
      log(0,2,"Listing VZ-1 patch bank files ");
      log(2,0,"...\n");
      this.bankDir = SYX.getPath(getClass(),"resources",SYX.SOURCE);
      log(2,0,"In folder: %s\n",this.bankDir);

      List<String> fList = getBankFiles(this.bankDir);
      for (int i=0; i<fList.size(); i++)
      {
        String name = fList.get(i).replace(".syx","");
        String id   = String.format("D(%02d)",i+1);
        this.patchBank.addBank(id,name);
        if (getVerbose()<2)
          log(0,0,".");
        log(2,0,"%s: %s\n",id,name);
      }
      if (getVerbose()<2)
        log(0,0," ");
      log(0,-2,"ok (%d file%s)\n",fList.size(),fList.size()!=1?"s":"");
    }
    catch (Exception e)
    {
      log(e);
    }
  }

  /**
   * Returns the number of available patch banks.
   */
  protected int getPatchBankCount()
  {
    return this.patchBank.getBanks(true).size();
  }

  /**
   * Returns a patch bank.
   * 
   * @param index
   *          The zero-based patch bank index
   */
  protected BankTree<VZ1Patch>.Bank getPatchBank(int index)
  {
    String UID = this.patchBank.getBankUIDs(true).get(index);
    return this.patchBank.getBank(UID,true);
  }

  /**
   * Returns the currently active patch bank.
   * 
   * @see #activePatchBank
   * @see #patchBank
   * @see #onCmdPActivatePatchBank(String)
   * @see #onCmdDPrintActivePatchBank(String)
   */
  protected BankTree<VZ1Patch>.Bank getActivePatchBank()
  {
    return this.patchBank.getBanks(true).get(this.activePatchBank);
  }

  /**
   * Returns the list of available SysEx bank dump files.
   * 
   * @param bankDir
   *          Directory containing the VZ bank dump files.
   */
  protected List<String> getBankFiles(String bankDir)
  {
    try
    {
      return
        Files.list(Paths.get(bankDir))
          .filter(file -> !Files.isDirectory(file))
          .map(Path::getFileName)
          .map(Path::toString)
          .collect(Collectors.toList());
    }
    catch (IOException e)
    {
      errlog(e);
      return new ArrayList<String>();
    }
  }

  /**
   * Loads a patch bank from a SysEx bank dump file.
   * 
   * <p>
   *   The path of the SysEx file to read is derived from the bank name as
   *   follows: {@code fname = }{@link Paths#get(String, String...)
   *   Paths.get}{@code (}{@link #bankDir}{@code ,bank.}{@link
   *   BankTree#getName() getName}{@code ()+".syx")}.
   * </p>
   * 
   * @param bank
   *          The patch bank to load. 
   */
  protected ErrorReport loadPatchBank(BankTree<VZ1Patch>.Bank bank)
  {
    ErrorReport er = new ErrorReport();
    bank.reset();

    String bname = bank.getName();
    String fname = (Paths.get(this.bankDir,bname+".syx")).toString();

    log(0,0,"Loading patch bank '%s' ",bank.getName());
    if (getVerbose()<2) log(0,0,".");
    log(2,2,"\n");
    log(2,0,  "%s\n",fname);
    SysexRecorder recorder = new SysexRecorder();
    try
    {
      // Read tape file
      log(2,0,"Reading tape file ...\n");
      recorder.readSyxFile(fname);
      int tlen = recorder.getTape().length;
      int blen = 0;
      for (int i=0; i<tlen; i++)
        blen += recorder.getTape()[i].getLength();
      log(2,0,"- Tape length is %d bytes",blen);
      log(2,0," in %d track%s (#0...#%d)\n",tlen,tlen!=1?"s":"",tlen-1);
      log(2,0,"- Payload track is #1\n");
      log(2,0,"ok\n");
      if (getVerbose()<2) log(0,0,".");
  
      // Create tape structure
      log(2,0,"Parsing payload track\n");
      log(2,0,"- Creating track model ...");
      SyxMessage sxTape = new SyxMessage();
      SyxDataStruct prt = new SyxDataStruct(VZ1_DATA_TAPE_HEADER);
      prt.setName("Track Header");
      sxTape.addParts(prt);
      for (int i=0; i<64; i++)
      {
        prt = new SyxDataStruct(VZ1_TD_TRACK);
        prt.setName(String.format("Tone Data %d",i));
        sxTape.addParts(prt);
      }
      for (int i=0; i<64; i++)
      {
        prt = new SyxDataStruct(VZ1_OP_TRACK);
        prt.setName(String.format("Operation Data #%02d",i));
        sxTape.addParts(prt);
      }
      log(2,0," ok\n");
      log
      (
        2,0,
        "- Track model length: %d bytes (%d parts)\n",
        sxTape.getMessage().length,
        sxTape.getParts().length
      );
      byte[] data = recorder.getTape()[1].getMessage();
      log(2,0,"- Tape track length : %d bytes\n",data.length);
      sxTape.setMessage(data,data.length);
      log(2,0,"ok\n");
      if (getVerbose()<2) log(0,0,".");

      // Payload track read-out (Tone Data and, pending, Operation Data)
      ArrayList<VZ1Patch> patches = new ArrayList<VZ1Patch>();
      log(2,2,"Reading 64 patches from payload track ");
      boolean bErr = false;
      for (int i=0; i<64; i++)
      {
        SyxDataStruct prtTd = sxTape.getPart(i+1);
        byte[]        datTd = (new SyxMessage(prtTd)).getData();
        VZ1Patch      patch = new VZ1Patch(datTd); 
        patches.add(patch);
        try
        {
          patch.validate();
        }
        catch (InvalidMidiDataException e)
        {
          bErr = true;
          er.nErrors++;
          er.messages.add(String.format("Patch '%s':",patch.getName()));
          er.messages.add(e.getMessage());
        }
        if (i%4==3)
          if (!bErr)
            log(0,0,".");
          else
          {
            bErr=false;
            errlog(0,0,".");
          }
      }
      log(2,-2,"ok\n");
      if (er.nErrors>0)
        errlog(2,0,er.getMessages()+"\n");

      log(2,0,"Writing patch bank ...");
      for (int i=0; i<patches.size(); i++)
        bank.addBankObject(VZ1.bankElemIndex2Id(i),patches.get(i));
      log(2,0," ok\n");
      if (getVerbose()<2) log(0,0,".");
      log(0,0," ");
      log(0,-2,"ok\n");
    }
    catch (Exception e)
    {
      log(e);
    }
    return er;
  }

  /**
   * Sends a patch to VZ.
   * 
   * @param patch
   *          The patch
   * @see AVZ1Appc#setAndSendPatch(VZ1Patch, int)
   */
  @Override
  protected void setAndSendPatch(VZ1Patch patch)
  {
    log(2,patch.prettyPrintModel()+"\n");
    super.setAndSendPatch(patch);
    prettyPrintPatchBank();
  }

  /**
   * Prints a list of available patch banks
   */
  protected void printAvailablePatchBanks()
  {

    int bCount = getPatchBankCount();
    for (int i=0; i<bCount; i++)
    {
      BankTree<VZ1Patch>.Bank bank = getPatchBank(i);
      String sA = i==this.activePatchBank ? "A" : " ";
      String sL = bank.getBankObjectUIDs().size()>0 ? "*" : " ";
      log(0,0,"- %02d%s%s %s\n",i+1,sL,sA,bank.getName());
    }
  }

  /**
   * Pretty-prints the current patch bank.
   */
  protected void prettyPrintPatchBank()
  {
    if (getActivePatchBank().getBankObjects().size()==0)
    {
      errlog(E_PATCHBANK_EMPTY+"\n",this.activePatchBank+1);
      return;
    }

    BankTree<VZ1Patch>.Bank bank = getActivePatchBank();
    log("-".repeat(115)+"\n");
    log("Patch Bank: %s\n",bank.getName());
    log("-".repeat(115)+"\n");
    List<BankTree<VZ1Patch>.BankObject> patches = bank.getBankObjects();
    for (int i=0; i<8; i++)
    {
      if (i==0)
      {
        log("  ");
        for (int j=0; j<8; j++)
          log("      %d       ",j+1);
        log("\n");
      }
      for (int j=0; j<8; j++)
      {
        if (j==0)
          log("%c  ",'A'+i);
        log("%-14s",patches.get(i*8+j).getName());
      }
      log("\n");
    }
    log("-".repeat(115)+"\n");
  }

  // == MAIN ==================================================================

  public static void main(String[] args)
  {
    VZ1PatchBankAppc theApp = new VZ1PatchBankAppc();
    theApp.run(true);
  }
}

// EOX