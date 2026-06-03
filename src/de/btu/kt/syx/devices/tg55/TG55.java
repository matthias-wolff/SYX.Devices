package de.btu.kt.syx.devices.tg55;

import java.util.concurrent.TimeoutException;
import java.util.stream.IntStream;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.SysexMessage;

import de.btu.kt.syx.SYX;
import de.btu.kt.syx.devices.AInstrument;
import de.btu.kt.syx.midi.SyxChecksum;
import de.btu.kt.syx.midi.SyxDataStruct;
import de.btu.kt.syx.midi.SyxMessage;
import de.btu.kt.syx.midi.SyxParamInfo;
import de.btu.kt.syx.util.SysexRecorder;

/**
 * Yamaha SY55/TG55 device model.
 * 
 * @author Matthias Wolff
 */
public class TG55 extends AInstrument
{
  private static final long serialVersionUID = 1L;

  // -- Constants -------------------------------------------------------------

  // Error Messages

  protected final String E_BULK_TYPEINVAL   // Invalid bulk type
    = "Invalid bulk type '%s'";
  protected final String E_SWITCH_ID        // Invalid switch ID
    = "Invalid switch ID 0x%02X";
  protected final String E_MEM_TYPE         // Invalid memory type
    = "Invalid memory type 0x%02X for bulk type '%s'";
  protected final String E_MEM_NUM          // Invalid memory number
  = "Invalid memory number 0x%02X for bulk type '%s'";

  // SysEx Message Formats

  public static final String F_ERRORINFO    // Error/status information
    = "43H 1#H 35H 7FH 00H[4] nnH";
  public static final String F_INF_BULKRCV  // Error/status information
  = "43H 1#H 35H 7FH 00H[4] 1EH";
  public static final String F_SWITCHREMOTE // Switch Remote
    = "43H 1#H 35H 0DH 00H[2] nnH 00H vvH";
  public static final String F_BULK_REQUEST // Bulk Request Message 
    = "43H 2#H 7AH 4CH 4DH 20H[2] 38H 31H 30H 33H ssH ttH 00H[14] xxH yyH";
  public static final String F_BD_HEADER1   // Bulk header 1
    = "43H 0#H 7AH bbH[2]";
  public static final String F_BD_HEADER2   // Bulk header 2
    = "4CH 4DH 20H[2] 38H 31H 30H 33H ssH ttH 00H[14] xxH yyH";
  public static final String F_BDMU_HEADER  // MU Bulk - Header
    = "aaH bbH ccH ddH eeH ffH ggH hhH iiH jjH 000sssss";
  public static final String F_BDMU_EFFECT  // MU Bulk - Effect
    = "ttH llH ppH qqH rrH";
  public static final String F_BDMU_VOICE   // MU Bulk - Voice
    = "0a000ooo 0000000m 00nnnnnn vvH ttH ssH 00pppppp eeH 000rrrrr";
  public static final String F_BDVC_HEADER  // VC Bulk - Header
    = "**H aaH bbH ccH ddH eeH ffH ggH hhH iiH jjH";

  // Switch IDs

  public static final int SW_VOICE        = 0x02;
  public static final int SW_EDIT_COMPARE = 0x04;
  public static final int SW_MEMORY       = 0x06;
  public static final int SW_SELECT       = 0x07;
  public static final int SW_EXIT         = 0x08;
  public static final int SW_ENTER        = 0x09;
  public static final int SW_DEMO         = 0x0D;
  public static final int SW_MULTI        = 0x11;
  public static final int SW_UTILITY      = 0x12;
  public static final int SW_PAGEUP       = 0x13;
  public static final int SW_RIGHT        = 0x15;
  public static final int SW_INC_YES      = 0x16;
  public static final int SW_XXX          = 0x17;
  public static final int SW_STORE_COPY   = 0x20;
  public static final int SW_PAGEDOWN     = 0x21;
  public static final int SW_LEFT         = 0x23;
  public static final int SW_DEC_NO       = 0x24;
  public static final int SW_INITIALSET   = 0x7F;

  // Part Indexes

  public static final int PRT_MU_COMMON = 2;
  public static final int PRT_MU_EFFECT = 3;
  public static final int PRT_MU_VOICES = 4;

  // Paramter UIDs
  public static final String UID_MUC_NAME(int i) {return String.format("MULTI_Name_%02d",i);}
  public static final String UID_MUE_SRC    = "EF.Source";
  public static final String UID_MUE_TYPE   = "EF.Type";
  public static final String UID_MUE_LEVEL  = "EF.Output_Level";
  public static final String UID_MUE_PARAM1 = "EF.Parameter1";
  public static final String UID_MUE_PARAM2 = "EF.Parameter2";
  public static final String UID_MUE_PARAM3 = "EF.Parameter3";
  public static final String UID_MUV_ENA    (int i) {return String.format("CH%02d.Enable"      ,i);}
  public static final String UID_MUV_OUTPUT (int i) {return String.format("CH%02d.Output_Asgn" ,i);}
  public static final String UID_MUV_VMEM   (int i) {return String.format("CH%02d.Voice_Memory",i);}
  public static final String UID_MUV_VNUM   (int i) {return String.format("CH%02d.Voice_Number",i);}
  public static final String UID_MUV_VOLUME (int i) {return String.format("CH%02d.Volume"      ,i);}
  public static final String UID_MUV_TUNE   (int i) {return String.format("CH%02d.Tune"        ,i);}
  public static final String UID_MUV_NSHIFT (int i) {return String.format("CH%02d.Note_Shift"  ,i);}
  public static final String UID_MUV_PAN    (int i) {return String.format("CH%02d.Pan"         ,i);}
  public static final String UID_MUV_EFLEVEL(int i) {return String.format("CH%02d.EF_Level"    ,i);}
  public static final String UID_MUV_RESNOTE(int i) {return String.format("CH%02d.ReserveNote" ,i);}

  // -- Attributes (TG55 instrument state) ------------------------------------
  
  /**
   * The multi edit buffer of the TG55 instrument. May be {@code null} if the
   * multi edit buffer has not been fetched from the instrument yet.
   */
  protected SyxMessage multiEditBuffer;

  /**
   * The patch bank.
   */
  protected TG55PatchBank tg55PatchBank;

  // -- Constructors ----------------------------------------------------------

  /**
   * Creates a new TG55 instrument wrapper.
   * 
   * @param devNum
   *          The {@linkplain #devNum device number} of the instrument (1, ...,
   *          {@link #getMaxDeviceNumber}{@code ()})
   * @param verbose
   *          Verbose level for console log, 0: silence
   * @throws IllegalArgumentException
   *          if {@code mi} is {@code null}, or if {@code devNum} is out of range
   */
  public TG55(int devNum, int verbose)
  throws IllegalArgumentException
  {
    super(devNum,verbose);
    this.multiEditBuffer = createMultiBulkDump();
    this.tg55PatchBank = new TG55PatchBank(
      new int[]    {0x00,0x02,0x7F}, // Memory type IDs
      new String[] {"I" ,"P", "E" }, // Memory type names
      new int[]    {  64,  64,   1}  // Bank sizes
    ); 
  }

  /**
   * Creates a new TG55 instrument wrapper.
   * 
   * @param devNum
   *          The {@linkplain #devNum device number} of the instrument (1, ...,
   *          {@link getMaxDeviceNumber}{@code ()})
   */
  public TG55(int devNum)
  throws IllegalArgumentException
  {
    this(devNum,0);
  }

  // -- Getters and Setters ---------------------------------------------------

  public TG55PatchBank getPatchBank()
  {
    return this.tg55PatchBank;
  }

  public SyxMessage getMultiEditBuffer()
  {
    return this.multiEditBuffer;
  }

  // -- MIDI SysEx API --------------------------------------------------------
  
  /**
   * Sends a switch remote message to the TG55 instrument.
   * 
   * @param n
   *          The switch number
   * @return The message
   * @throws IllegalArgumentException
   *          if any argument or the combination of arguments is not permissible
   * @throws MidiUnavailableException
   *           if the MIDI interface is unavailable
   * @see #createSwitchRemoteMsg(int,boolean)
   */
  public void sendSwitchRemote(int n)
  throws IllegalArgumentException, MidiUnavailableException
  {
    sendSysexMsg(createSwitchRemoteMsg(n,true));
  }

  /**
   * Sends a multi bulk request message to the TG55 instrument.
   * 
   * @param x
   *          The memory type
   * @param y
   *          The memory number)
   * @return The message
   * @throws IllegalArgumentException
   *          if any argument or the combination of arguments is not permissible
   * @throws MidiUnavailableException
   *           if the MIDI interface is unavailable
   */
  public void sendMultiBulkRequest(int x, int y)
  throws IllegalArgumentException, MidiUnavailableException
  {
    sendSysexMsg(createBulkRequest("MU",x,y));
  }

  /**
   * Sends a voice bulk request message to the TG55 instrument.
   * 
   * @param x
   *          The memory type
   * @param y
   *          The memory number)
   * @return The message
   * @throws IllegalArgumentException
   *          if any argument or the combination of arguments is not permissible
   * @throws MidiUnavailableException
   *           if the MIDI interface is unavailable
   */
  public void sendVoiceBulkRequest(int x, int y)
  throws IllegalArgumentException, MidiUnavailableException
  {
    sendSysexMsg(createBulkRequest("VC",x,y));
  }

  /**
   * Sends the multi edit buffer to the TG55 instrument.
   * 
   * @throws MidiUnavailableException
   *           if the MIDI interface is unavailable
   */
  public void sendMultiEditBuffer() 
  throws MidiUnavailableException
  {
    try
    {
      long then = System.nanoTime();
      log(1,2,"Sending multi edit buffer ... ");
      String format = F_INF_BULKRCV;
      fetchSysexMsg(createMultiBulkDump(this.multiEditBuffer,false),format);
      sendSwitchRemote(SW_EXIT);
      log(1,-2,"ok (%.1f s)\n",(System.nanoTime()-then)/1000000000.);
    }
    catch (InvalidMidiDataException e)
    { // Cannot happen
      e.printStackTrace();
    }
  }

  /**
   * Fetches the multi edit buffer from the TG55 instrument.
   * 
   * @throws IllegalStateException
   *           if another fetch is pending
   * @throws MidiUnavailableException
   *           if the MIDI interface is unavailable
   * @throws TimeoutException
   *           if the bulk request timed out, see {@link AInstrument#TIMEOUT}
   */
  public void fetchMultiEditBuffer() 
  throws IllegalStateException, MidiUnavailableException, TimeoutException
  {
    try
    {
      long then = System.nanoTime();
      log(1,2,"Fetching multi edit buffer ... ");
      sendSwitchRemote(SW_MULTI);
      String format = F_BD_HEADER1+" "+F_BD_HEADER2;
      SysexMessage mRsp = fetchSysexMsg(createBulkRequest("MU",0x7F,0x00),format);
      SyxMessage sxRsp = createMultiBulkDump(mRsp,true);
      this.multiEditBuffer.setMessage(sxRsp.getMessage(),sxRsp.getLength());
      log(1,-2,"ok (%.1f s)\n",(System.nanoTime()-then)/1000000000.);
    }
    catch (InvalidMidiDataException e)
    { // Cannot happen
      e.printStackTrace();
    }
  }

  /**
   * Fetches all patch names from the TG55 instrument.
   * 
   * @throws IllegalStateException
   *          if another fetch is pending
   * @throws MidiUnavailableException
   *          if the MIDI interface is unavailable
   * @throws TimeoutException
   *          if a bulk request timed out, see {@link AInstrument#TIMEOUT}
   */
  public void fetchPatchNames() 
  throws IllegalStateException, MidiUnavailableException, TimeoutException
  {
    long then = System.nanoTime();
    log(1,"Fetching patch names");
    for (int m : this.tg55PatchBank.getBankIDs())
    {
      log(1,"\n  ");
      for (int n=0; n<this.tg55PatchBank.getBankSize(m); n++)
        try
        {
          this.tg55PatchBank.setPatchName(m,n,fetchPatchName(m,n));
          log(1,".");
        }
        catch (IllegalArgumentException e)
        { // Cannot happen
          e.printStackTrace();
        }
    }
    log(1,"\nok (%.1f s)\n",(System.nanoTime()-then)/1000000000.);
  }

  /**
   * Fetches a patch name from from the TG55 instrument.
   * 
   * @param m
   *          The memory type (0x00: internal, 0x02: preset, 0x7F: edit buffer)
   * @param n
   *          The memory number (0x00...0x3F)
   * @return  The requested patch name
   * @throws IllegalArgumentException
   *          if either argument is out of range
   * @throws IllegalStateException
   *          if another fetch is pending
   * @throws MidiUnavailableException
   *          if the MIDI interface is unavailable
   * @throws TimeoutException
   *          if the bulk request timed out, see {@link AInstrument#TIMEOUT}
   */
  public String fetchPatchName(int m, int n)
  throws IllegalArgumentException, IllegalStateException,
         MidiUnavailableException, TimeoutException
  {
    try
    {
      String format = F_BD_HEADER1+" "+F_BD_HEADER2;
      SysexMessage ret = fetchSysexMsg(createBulkRequest("VC",m,n),format);
      SyxMessage vcm = createVoiceBulkDump(ret);
      SyxDataStruct ds = vcm.getPart(2);
      String name = "";
      for (char c='a'; c<='j'; c++)
        name += (char)ds.getMidiValue(c);
      return name;
    }
    catch (InvalidMidiDataException e)
    { // Cannot happen
      e.printStackTrace();
      return null;
    }
  }

  // -- TG55 SysEx Implementation ---------------------------------------------

  /**
   * Creates a switch remote message.
   * <p>See TG55 Operating Manual, appendix "Midi Data Format", chart 7.</p> 
   * 
   * @param n
   *          The switch number
   * @param v
   *          {@code true} for on, {@code false} for off   
   * @return The message
   * @throws IllegalArgumentException
   *          if any argument or the combination of arguments is not permissible
   */
  protected SyxMessage createSwitchRemoteMsg(int n, boolean v)
  throws IllegalArgumentException
  {
    // Sanity checks
    byte[] switches = new byte[]
    {
      0x03, 0x04, 0x06, 0x07, 0x08, 0x09, 0x0D, 0x11, 0x12, 0x13, 0x15, 0x16,
      0x17, 0x20, 0x21, 0x23, 0x24, 0x7F
    };
    boolean found = false;
    for (byte sw : switches)
      if (sw==n)
      {
        found = true;
        break;
      }
    if (!found)
      throw SYX.IllArgExc(SYX.ERR(E_SWITCH_ID,n));

    // Create message
    try
    {
      SyxDataStruct ds = new SyxDataStruct(F_SWITCHREMOTE);
      ds.setMidiValue('#',this.devNum-1);
      ds.setMidiValue('n',n            );
      ds.setMidiValue('v',v?0x40:0x00  );
      return new SyxMessage(ds);
    }
    catch (InvalidMidiDataException e)
    { // Cannot happen
      e.printStackTrace();
      return null;
    }
  }

  protected SyxMessage createErrorInfo(SysexMessage sxMsg)
  throws InvalidMidiDataException
  {
    // Interpret data
    SyxMessage vcd = new SyxMessage();
    vcd.addParts(new SyxDataStruct(F_ERRORINFO,"Error info"));
    vcd.setMessage(sxMsg.getMessage(),vcd.getLength());

    // Check device ID
    if (vcd.getPart(0).getMidiValue('#')+1!=this.devNum)
      throw new InvalidMidiDataException();

    return vcd;
  }

  /**
   * Creates a bulk request message.
   * <p>See TG55 Operating Manual, appendix "Midi Data Format", chart 11.</p> 
   * 
   * @param type
   *          The bulk type ("VC": voice, "MU": multi, or "SY": system)
   * @param m
   *          The memory type
   * @param n
   *          The memory number
   * @return The message
   * @throws IllegalArgumentException
   *          if any argument or the combination of arguments is not permissible
   */
  protected SyxMessage createBulkRequest(String type, int m, int n)
  throws IllegalArgumentException
  {
    // Sanity checks
    if (!"VC".equals(type) && !"MU".equals(type) && !"SY".equals(type))
      throw SYX.IllArgExc(SYX.ERR(E_BULK_TYPEINVAL,type));
    if ("VC".equals(type) || "MU".equals(type))
      if (m!=0x00 && m!=0x02 && m!=0x7F)
        throw SYX.IllArgExc(SYX.ERR(E_MEM_TYPE,m,type));
    if ("SY".equals(type) && m!=0x00)
        throw SYX.IllArgExc(SYX.ERR(E_MEM_TYPE,m,type));
    if ("VC".equals(type) && (n<0x00 || n>0x3F))
      throw SYX.IllArgExc(SYX.ERR(E_MEM_NUM,n,type));
    if ("MU".equals(type) && (n<0x00 || n>0x0F))
      throw SYX.IllArgExc(SYX.ERR(E_MEM_NUM,n,type));
    if ("SY".equals(type) && n!=0x00)
      throw SYX.IllArgExc(SYX.ERR(E_MEM_NUM,n,type));

    // Create message
    try
    {
      SyxDataStruct ds = new SyxDataStruct(F_BULK_REQUEST);
      ds.setMidiValue('#',this.devNum-1 );
      ds.setMidiValue('s',type.charAt(0));
      ds.setMidiValue('t',type.charAt(1));
      ds.setMidiValue('x',m             );
      ds.setMidiValue('y',n             );
      return new SyxMessage(ds);
    }
    catch (InvalidMidiDataException e)
    { // Cannot happen
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Creates a TG55 multi bulk dump message.
   * <p>See TG55 Operating Manual, appendix "Midi Data Format", section 
   * (3-3-2).</p> 
   */
  protected SyxMessage createMultiBulkDump()
  {
    SyxDataStruct prt;
    SyxParamInfo  pi;

    try
    {
      // -- Create SyxMessage -------------------------------------------------
      SyxMessage mub = new SyxMessage();

      // -  Parts 0,1: Bulk Headers - - - - - - - - - - - - - - - - - - - - - -
      mub.addParts(new SyxDataStruct(F_BD_HEADER1,"Bulk Header 1"));
      mub.addParts(new SyxDataStruct(F_BD_HEADER2,"Bulk Header 2"));

      // -  Part 2: Multi Common  - - - - - - - - - - - - - - - - - - - - - - -
      // Create part
      prt = new SyxDataStruct(F_BDMU_HEADER,"Multi Common");

      // Info for parameters 'a'...'j': Multi Name
      for (int i=0; i<10; i++)
      {
        char pname = (char)('a'+i);
        pi = new SyxParamInfo(prt,pname,UID_MUC_NAME(i+1),"Multi Name");
        pi.setValueRange(0x20,0x7F,0);
        pi.setParamChangeMsgFormat(getParamChangeMsgFormat
        (// StrNum    ParNum ParVal
            0x00,0x00,0x00,i,String.format("00H %c%cH",pname,pname)
        ));
        prt.addParamInfo(pi);
      }

      // Info for parameter 's': Effect Source
      pi = new SyxParamInfo(prt,'s',UID_MUE_SRC,"Effect Source");
      pi.setValueMap
      (
        IntStream.rangeClosed(0,16).toArray(),
        new String[] 
        {
          "multi",                                                     // 00
          "CH1", "CH2" , "CH3" , "CH4" , "CH5" , "CH6" , "CH7" , "CH8",// 01...08
          "CH9", "CH10", "CH11", "CH12", "CH13", "CH14", "CH15", "CH16"// 09...16
        }
      );
      pi.setParamChangeMsgFormat(getParamChangeMsgFormat
      (// StrNum    ParNum    ParVal
          0x00,0x00,0x00,0x0A,"00H 000sssss"
      ));
      prt.addParamInfo(pi);

      // Add part
      prt.doAssertAllParamInfos();
      mub.addParts(prt);
  
      // -  Part 3: Multi Effect  - - - - - - - - - - - - - - - - - - - - - - -
      // Create part
      prt = new SyxDataStruct(F_BDMU_EFFECT,"Multi Effect");

      // Info for parameter 't': Type
      pi = new SyxParamInfo(prt,'t',UID_MUE_TYPE,"Type");
      pi.setValueMap
      (
        IntStream.rangeClosed(1,34).toArray(),
        new String[] 
        {
          "Rev.Hall", "Rev.Room", "RevPlate", "RevChurch", "Rev.Club", // 01...05
          "RevStage", "BathRoom", "RevMetal", "Delay"    , "DelayL/R", // 06...10
          "St.Echo" , "Doubler1", "Doubler2", "PingPong" , "PanRef." , // 11...15
          "EarlyRef", "GateRev" , "Rvs Gate", "FB E/R"   , "FB Gate" , // 16...20
          "FB Rvs"  , "Dly1&Rev", "Dky2&Rev", "Tunnel"   , "Tone 1"  , // 21...25
          "Dly1&T1" , "Dly2&T1" , "Tone 2"  , "Dly1&T2"  , "Dly2&T2" , // 26...30
          "Dst&Rev" , "Dst&Dly1", "Dst&Dly2", "Dist."                  // 31...34
        }
      );
      pi.setDefaultMidiValue(1);
      pi.setParamChangeMsgFormat(getParamChangeMsgFormat
      (// StrNum    ParNum    ParVal
          0x08,0x00,0x00,0x00,"00H ttH"
      ));
      prt.addParamInfo(pi);

      // Info for parameter 'l': Output Level
      pi = new SyxParamInfo(prt,'l',UID_MUE_LEVEL,"Output Level");
      pi.setValueRange(0,100,0);
      pi.setDefaultMidiValue(0);
      pi.setParamChangeMsgFormat(getParamChangeMsgFormat
      (// StrNum    ParNum    ParVal
          0x08,0x00,0x00,0x01,"00H llH"
      ));
      prt.addParamInfo(pi);

      // Info for parameter 'p': Parameter 1
      pi = new SyxParamInfo(prt,'p',UID_MUE_PARAM1,"Parameter 1");
      pi.setValueRange(0,127,0);
      pi.setDefaultMidiValue(0);
      pi.setParamChangeMsgFormat(getParamChangeMsgFormat
      (// StrNum    ParNum    ParVal       
          0x08,0x00,0x00,0x02,"00H ppH"
      ));
      prt.addParamInfo(pi);

      // Info for parameter 'q': Parameter 2
      pi = new SyxParamInfo(prt,'q',UID_MUE_PARAM2,"Parameter 2");
      pi.setValueRange(0,127,0);
      pi.setDefaultMidiValue(0);
      pi.setParamChangeMsgFormat(getParamChangeMsgFormat
      (
        0x08,0x00,0x00,0x03,"00H qqH"
      ));
      prt.addParamInfo(pi);

      // Info for parameter 'r': Parameter 3
      pi = new SyxParamInfo(prt,'r',UID_MUE_PARAM3,"Parameter 3");
      pi.setValueRange(0,127,0);
      pi.setDefaultMidiValue(0);
      pi.setParamChangeMsgFormat(getParamChangeMsgFormat
      (// StrNum    ParNum    ParVal
          0x08,0x00,0x00,0x04,"00H rrH"
      ));
      prt.addParamInfo(pi);

      // Add part
      prt.doAssertAllParamInfos();
      mub.addParts(prt);

      // -  Part 4-19: Voice  - - - - - - - - - - - - - - - - - - - - - - - - -
      for (int i=0; i<16; i++)
      {
        // Create part
        String name = String.format("Channel %d Voice",i+1);
        prt = new SyxDataStruct(F_BDMU_VOICE,name);

        // Info for parameter 'a': Enabled
        pi = new SyxParamInfo(prt,'a',UID_MUV_ENA(i+1),"Enabled");
        pi.setValueMap(new int[]{0,1},new String[]{"off","on"});
        pi.setDefaultMidiValue(0);
        pi.setParamChangeMsgFormat(getParamChangeMsgFormat
        (// StrNum ParNum    ParVal
            0x01,i,0x00,0x00,"00H 0a000ooo"
        ));
        prt.addParamInfo(pi);

        // Info for parameter 'o': Output
        pi = new SyxParamInfo(prt,'o',UID_MUV_OUTPUT(i+1),"Output");
        pi.setValueMap
        (
          IntStream.rangeClosed(0,5).toArray(),
          new String[]{"str","-:-","1:-","-:2","1:2","vce"}
        );
        pi.setDefaultMidiValue(0);
        pi.setParamChangeMsgFormat(getParamChangeMsgFormat
        (// StrNum ParNum    ParVal
            0x01,i,0x00,0x00,"00H 0a000ooo"
        ));
        prt.addParamInfo(pi);

        // Info for parameter 'm': Voice Memory
        pi = new SyxParamInfo(prt,'m',UID_MUV_VMEM(i+1),"Voice Memory");
        pi.setValueMap(new int[] {0,1},new String[]{"int/crd","pre"});
        pi.setDefaultMidiValue(0);
        pi.setParamChangeMsgFormat(getParamChangeMsgFormat
        (// StrNum ParNum    ParVal
            0x01,i,0x00,0x01,"00H 0000000m"
        ));
        prt.addParamInfo(pi);

        // Info for parameter 'n': Voice Number
        pi = new SyxParamInfo(prt,'n',UID_MUV_VNUM(i+1),"Voice Number");
        pi.setValueRange(0,63,0);
        pi.setDefaultMidiValue(0);
        pi.setParamChangeMsgFormat(getParamChangeMsgFormat
        (// StrNum ParNum    ParVal
            0x01,i,0x00,0x02,"00H 00nnnnnn"
        ));
        prt.addParamInfo(pi);

        // Info for parameter 'v': Volume
        pi = new SyxParamInfo(prt,'v',UID_MUV_VOLUME(i+1),"Volume");
        pi.setValueRange(0,127,0);
        pi.setDefaultMidiValue(127);
        pi.setParamChangeMsgFormat(getParamChangeMsgFormat
        (// StrNum ParNum    ParVal
            0x01,i,0x00,0x03,"00H vvH"
        ));
        prt.addParamInfo(pi);

        // Info for parameter 't': Tuning
        pi = new SyxParamInfo(prt,'t',UID_MUV_TUNE(i+1),"Tuning");
        pi.setValueRange(0,127,-64);
        pi.setDefaultModelValue(0);
        pi.setParamChangeMsgFormat(getParamChangeMsgFormat
        (// StrNum ParNum    ParVal
            0x01,i,0x00,0x04,"00H ttH"
        ));
        prt.addParamInfo(pi);

        // Info for parameter 's': Note Shift
        pi = new SyxParamInfo(prt,'s',UID_MUV_NSHIFT(i+1),"Note Shift");
        pi.setValueRange(0,127,-64);
        pi.setDefaultModelValue(0);
        pi.setParamChangeMsgFormat(getParamChangeMsgFormat
        (// StrNum ParNum    ParVal
            0x01,i,0x00,0x05,"00H ssH"
        ));
        prt.addParamInfo(pi);

        // Info for parameter 'p': Pan
        pi = new SyxParamInfo(prt,'p',UID_MUV_PAN(i+1),"Pan");
        pi.setValueRange(0,63,-32);
        pi.setDefaultModelValue(0);
        pi.setParamChangeMsgFormat(getParamChangeMsgFormat
        (// StrNum ParNum    ParVal
            0x01,i,0x00,0x06,"00H 00pppppp"
        ));
        prt.addParamInfo(pi);

        // Info for parameter 'e': Effect Level
        pi = new SyxParamInfo(prt,'e',UID_MUV_EFLEVEL(i+1),"Effect Level");
        pi.setValueRange(0,100,0);
        pi.setDefaultMidiValue(100);
        pi.setParamChangeMsgFormat(getParamChangeMsgFormat
        (// StrNum ParNum    ParVal
            0x01,i,0x00,0x07,"00H eeH"
        ));
        prt.addParamInfo(pi);

        // Info for parameter 'r': Reserve Note
        pi = new SyxParamInfo(prt,'r',UID_MUV_RESNOTE(i+1),"Reserve Note");
        pi.setValueRange(0,16,0);
        pi.setDefaultMidiValue(0);
        pi.setParamChangeMsgFormat(getParamChangeMsgFormat
        (// StrNum ParNum    ParVal
            0x01,i,0x00,0x08,"00H 000rrrrr"
        ));
        prt.addParamInfo(pi);

        // Add part
        prt.doAssertAllParamInfos();
        mub.addParts(prt);
      }

      // -  Part 20: Checksum - - - - - - - - - - - - - - - - - - - - - - - - -
      mub.addParts(new SyxChecksum(SyxChecksum.ROLAND,1,-1));

      // -- Initialize data ---------------------------------------------------
      mub.reset();

      // Return SysEx message
      return mub;
    }
    catch (InvalidMidiDataException e)
    {// Should not happen
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Interprets a MIDI system exclusive message as a TG55 multi bulk message.
   * <p>See TG55 Operating Manual, appendix "Midi Data Format", section 
   * (3-3-2).</p> 
   * 
   * @param sxMsg
   *          The MIDI system exclusive message
   * @param incoming
   *          <ul>
   *            <li>If {@code true}, {@code sxMsg} will be treated as an 
   *            incoming message, i.e., the method will validate the checksum 
   *            and the {@linkplain #getDevNum() device number}.</li>
   *            <li>If {@code false}, {@code sxMsg} will be treated as an 
   *            outgoing message, i.e., the method will compute the checksum and
   *            write it into the message.</li>
   *          </ul> 
   * @return The TG55 multi bulk message
   * @throws InvalidMidiDataException
   *          if {@code msg} does not contain TG55 multi bulk data 
   */
  protected SyxMessage createMultiBulkDump(SysexMessage sxMsg, boolean incoming)
  throws InvalidMidiDataException
  {
    SyxMessage mub = createMultiBulkDump();
    mub.setMessage(sxMsg.getMessage(),mub.getLength());

    if (incoming)
    {
      // Validate checksum
      SyxChecksum cs = (SyxChecksum)mub.getPart(mub.getParts().length-1);
      if (cs.getCheckSum()!=cs.computeCheckSum(mub))
        throw new InvalidMidiDataException(E_CHECKSUM);

      // Check device ID
      if (mub.getParts()[0].getMidiValue('#')+1!=this.devNum)
        throw new InvalidMidiDataException();
    }
    else
    {
      // Write checksum
      SyxChecksum cs = (SyxChecksum)mub.getPart(mub.getParts().length-1);
      cs.setCheckSum(cs.computeCheckSum(mub));
    }

    // Ok
    return mub;
  }

  /**
   * Interprets a MIDI system exclusive message as a TG55 voice bulk message.
   * <p>See TG55 Operating Manual, appendix "Midi Data Format", section 
   * (3-3-2).</p> 
   * 
   * @param sxMsg
   *          The MIDI system exclusive message
   * @return The TG55 voice bulk message
   * @throws InvalidMidiDataException
   *          if {@code msg} does not contain TG55 voice bulk data 
   */
  protected SyxMessage createVoiceBulkDump(SysexMessage sxMsg)
  throws InvalidMidiDataException
  {
    // Get data byte count
    SyxMessage vcd = new SyxMessage();
    vcd.addParts(new SyxDataStruct(F_BD_HEADER1,"Bulk Header 1"));
    vcd.setMessage(sxMsg.getMessage(),vcd.getLength());
    int byteCount = vcd.getPart(0).getMidiValue('b');

    // Interpret data
    vcd = new SyxMessage();
    vcd.addParts(new SyxDataStruct(F_BD_HEADER1 ,"Bulk Header 1"));
    vcd.addParts(new SyxDataStruct(F_BD_HEADER2 ,"Bulk Header 2"));
    vcd.addParts(new SyxDataStruct(F_BDVC_HEADER,"Voice Header" ));
    byteCount -= vcd.getPart(1).getLength(); 
    byteCount -= vcd.getPart(2).getLength(); 
    vcd.addParts(new SyxDataStruct("**H ".repeat(byteCount),"(Other Data)"));
    SyxChecksum cs = new SyxChecksum(SyxChecksum.ROLAND,1,-1);
    vcd.addParts(cs);
    vcd.setMessage(sxMsg.getMessage(),vcd.getLength());

    // Validate checksum
    if (cs.getCheckSum()!=cs.computeCheckSum(vcd))
      throw new InvalidMidiDataException(E_CHECKSUM);

    // Check device ID
    if (vcd.getParts()[0].getMidiValue('#')+1!=this.devNum)
      throw new InvalidMidiDataException();

    return vcd;
  }

  /**
   * Creates a {@linkplain SyxDataStruct format specifier} for a system 
   * exclusive parameter change message.
   * <p>See TG55 Operating Manual, appendix "Midi Data Format", section (3-3-1)
   * </p> 
   * 
   * @param strNumMsb
   *          Structure number MSB
   * @param strNumLsb
   *          Structure number LSB
   * @param parNumMsb
   *          Parameter number MSB
   * @param parNumLsb
   *          Parameter number LSB
   * @param parValFormat
   *          {@linkplain SyxDataStruct Format specifier} for the parameter
   *          value
   * @return The message format specifier
   */
  protected String getParamChangeMsgFormat
  (
    int    strNumMsb,
    int    strNumLsb,
    int    parNumMsb,
    int    parNumLsb,
    String parValFormat
  )
  {
    String s = "43H 1#H 35H ssH[2] ppH[2] vvH[2]";
    s = s.replace("ssH[2]",String.format("%02XH %02XH",strNumMsb,strNumLsb));
    s = s.replace("ppH[2]",String.format("%02XH %02XH",parNumMsb,parNumLsb));
    s = s.replace("vvH[2]",parValFormat);
    return s;
  }

  // -- Implementation of AInstrument -----------------------------------------

  @Override
  public void processSysexMsg(SysexMessage sxMsg)
  throws InvalidMidiDataException
  {
    if (isFetchPending())
      log(1,"\n");
    log(1,2,"Processing SysEx message ...");
    //ORK.printStackTrace("");
    try
    {
      sxMsg = createMultiBulkDump(sxMsg,true);
      this.multiEditBuffer.setMessage(sxMsg.getMessage(),sxMsg.getLength());
      log(1,-2," ACCEPTED");
      if (isFetchPending())
        log(1,"\n");
    }
    catch (Exception e1)
    {
      try
      {
        SyxMessage msg = createErrorInfo(sxMsg);
        int code = msg.getPart(0).getMidiValue('n');
        String descr = getErrorInfoDescr(code);
        log(1,-2," ACCEPTED, Error info %02X (%s)",code,descr);
        if (isFetchPending())
          log(1,"\n");
      }
      catch (Exception e2)
      {
        log(e2);
        log(1,-2," REJECTED\n");
        throw e2;
      }
    }
  }

  @Override
  public void record(SysexRecorder recorder)
  {
    try
    {
      SyxMessage sxMsg = createMultiBulkDump(this.multiEditBuffer,false);
      recorder.record(sxMsg);
    }
    catch (Exception e)
    {// Should not happen
      e.printStackTrace();
    }
  }

  @Override
  public String prettyPrint()
  {
    String s = String.format("Device State of %s:\n",getLogID());

//    // Patch names
//    s += "- Patch names:\n";
//    s += "    "+this.patchBank.prettyPrint("    ");
//    s += "\n";

    // Multi edit buffer
    try
    {
      SyxDataStruct prt;
      SyxParamInfo  pi;
      String        sv;
      int           iv;
      s += "- Multi edit buffer:\n";
  
      // - Multi common
      prt = this.multiEditBuffer.getPart(2); 
      s += String.format("  - %s\n",prt.getName());

      sv = prt.readString("a-j");
      pi = prt.getParamInfo('a');
      s += String.format("    %-13s: '%s'\n",pi.getDescr(),sv);

      iv = prt.getMidiValue('s');
      pi = prt.getParamInfo('s');
      sv = pi.midi2ModelAsString(iv);
      s += String.format("    %-13s: %3d ('%s')\n",pi.getDescr(),iv,sv);

      // Multi effect
      prt = this.multiEditBuffer.getPart(3); 
      s += String.format("  - %s\n",prt.getName());

      pi = prt.getParamInfo('t');
      iv = prt.getMidiValue('t');
      sv = pi.midi2ModelAsString(iv);
      s += String.format("    %-13s: %3d ('%s')\n",pi.getDescr(),iv,sv);

      for (char c : new char[] {'l','p','q','r'})
      {
        pi = prt.getParamInfo(c);
        iv = prt.getMidiValue(c);
        s += String.format("    %-13s: %3d\n",pi.getDescr(),iv);
      }

      // Multi voices
      s += "  - Multi Voices  ";
      for (int i=0; i<16; i++)
        s += String.format(" CH%02d",i+1);
      s += String.format("\n    %-13s:","Output");
      for (int i=0; i<16; i++)
      {
        prt = this.multiEditBuffer.getPart(4+i); 
        boolean on = prt.getMidiValue('a')==1;
        iv = prt.getMidiValue('o');
        sv = on ? prt.getParamInfo('o').midi2ModelAsString(iv) : "----";
        s += String.format(" %4s",sv);
      }
      s += String.format("\n    %-13s:","Patch");
      for (int i=0; i<16; i++)
      {
        prt = this.multiEditBuffer.getPart(4+i); 
        boolean on = prt.getMidiValue('a')==1;
        String  sm = prt.getMidiValue('m')==0 ? "I" : "P";
        String  sn = String.format("%02d",prt.getMidiValue('n')+1);
        s += String.format(" %4s",on ? sm+sn : "");
      }
      s += String.format("\n    %-13s:","Reserve Note");
      for (int i=0; i<16; i++)
      {
        prt = this.multiEditBuffer.getPart(4+i); 
        boolean on = prt.getMidiValue('a')==1;
        iv = prt.getMidiValue('r');
        s += String.format(" %4s",on ? String.valueOf(iv) : "");
      }
      s += String.format("\n    %-13s:","Tuning");
      for (int i=0; i<16; i++)
      {
        prt = this.multiEditBuffer.getPart(4+i); 
        boolean on = prt.getMidiValue('a')==1;
        pi = prt.getParamInfo('t');
        iv = pi.midi2Model(prt.getMidiValue('t'));
        sv = iv==0 ? "0" : String.format("%+d",iv);
        s += String.format(" %4s",on ? sv : "");
      }
      s += String.format("\n    %-13s:","Note Shift");
      for (int i=0; i<16; i++)
      {
        prt = this.multiEditBuffer.getPart(4+i); 
        boolean on = prt.getMidiValue('a')==1;
        pi = prt.getParamInfo('s');
        iv = pi.midi2Model(prt.getMidiValue('s'));
        sv = iv==0 ? "0" : String.format("%+d",iv);
        s += String.format(" %4s",on ? sv : "");
      }
      s += String.format("\n    %-13s:","Pan");
      for (int i=0; i<16; i++)
      {
        prt = this.multiEditBuffer.getPart(4+i); 
        boolean on = prt.getMidiValue('a')==1;
        pi = prt.getParamInfo('p');
        iv = pi.midi2Model(prt.getMidiValue('p'));
        sv = iv==-64 ? "vce" : (iv==0 ? "0" : String.format("%+d",iv));
        s += String.format(" %4s",on ? sv : "");
      }
      s += String.format("\n    %-13s:","Effect Level");
      for (int i=0; i<16; i++)
      {
        prt = this.multiEditBuffer.getPart(4+i); 
        boolean on = prt.getMidiValue('a')==1;
        iv = prt.getMidiValue('e');
        s += String.format(" %4s",on ? String.valueOf(iv) : "");
      }
      s += String.format("\n    %-13s:","Volume");
      for (int i=0; i<16; i++)
      {
        prt = this.multiEditBuffer.getPart(4+i); 
        boolean on = prt.getMidiValue('a')==1;
        iv = prt.getMidiValue('v');
        s += String.format(" %4s",on ? String.valueOf(iv) : "");
      }
    }
    catch (Exception e)
    { // Cannot happen
      e.printStackTrace();
    }

    return s;
  }

  @Override
  public String prettyPrintDataStructure()
  {
    String s = "Data Structure of TG55 :\n";

    // Multi edit buffer
    try
    {
      s += "- Multi edit buffer:\n";
      for (int ip=0; ip<this.multiEditBuffer.getParts().length; ip++)
      {
        SyxDataStruct prt = this.multiEditBuffer.getPart(ip);
        s += prt.prettyPrint();
      }
    }
    catch (Exception e)
    { // Cannot happen
      e.printStackTrace();
    }

    return s;
  }

  // -- Implementation of ILooger ---------------------------------------------
  
  /**
   * Returns "TG55 #&lang;{@link #getDevNum()}&rang;".
   */
  @Override
  public String getLogID()
  {
    return String.format("TG55 #%02d",this.devNum);
  }

  // -- More Helpers ----------------------------------------------------------

  /**
   * Returns a TG55 patch ID '<tt>[I|P]&lt;nn&gt;</tt>'.
   *  
   * @param m 
   *          Memory type (0x00: internal, 0x02: preset/card, 0x7F: edit buffer)
   * @param n 
   *          Memory number (one-based)
   */
  protected static String makePatchID(int m, int n)
  {
    String t = "?";
    switch (m)
    {
    case 0x00: t = "I"; break; 
    case 0x02: t = "P"; break; 
    case 0x7F: t = "E"; break; 
    }
    return String.format("%s%02d",t,n);    
  }

  protected String getErrorInfoDescr(int code)
  {
    switch(code)
    {
    case 0x01: return "MIDI Buffer Full";
    case 0x02: return "SEQ Buffer Full";
    case 0x03: return "MIDI Data";
    case 0x04: return "MIDI Check Sum";
    case 0x05: return "MIDI Device# off";
    case 0x06: return "MIDI Bulk Prot.";
    case 0x07: return "No Data Card";
    case 0x08: return "Data Card Prot.";
    case 0x09: return "Data Card Format";
    case 0x0A: return "Illegal Data";
    case 0x0B: return "Verify Failed";
    case 0x0C: return "Internal Bat.Lo";
    case 0x0D: return "Data Card Bat.LO";
    case 0x0E: return "SEQ Memory Full";
    case 0x0F: return "SEQ Data Empty";
    case 0x10: return "Now SEQ Running";
    case 0x11: return "Song Data Exist";
    case 0x12: return "Internal Bat.NG";
    case 0x13: return "Data Card Bat.NG";
    case 0x14: return "ID Mismatch";
    case 0x15: return "No Wave Card";
    case 0x16: return "Wrong Wave Card";
    case 0x17: return "Now SEQ Running";
    case 0x19: return "Voice Type";
    case 0x1A: return "Song Cleared";
    case 0x1E: return "Bulk Received";
    case 0x1F: return "Bulk Receiving";
    case 0x20: return "Bulk Canceled";
    default  : return "(not defined)";
    }
  }
  
}

// EOF