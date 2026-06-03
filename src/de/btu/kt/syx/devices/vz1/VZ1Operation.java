package de.btu.kt.syx.devices.vz1;

import javax.sound.midi.InvalidMidiDataException;

import de.btu.kt.syx.SYX;
import de.btu.kt.syx.devices.IMulti;
import de.btu.kt.syx.midi.SyxDataStruct;
import de.btu.kt.syx.midi.SyxMessage;
import de.btu.kt.syx.midi.SyxParamInfo;

/**
 * A VZ-1/VZ-10M Operation Data model. 
 *
 * @author Matthias Wolff
 * 
 * @see VZ-1/VZ-10M MIDI SysEx manual, section III-3
 */
public class VZ1Operation extends SyxMessage implements IMulti
{
  private static final long serialVersionUID = 1L;

  // -- Constants -------------------------------------------------------------

  // -  Error Messages  - - - - - - - - - - - - - - - - - - - - - - - - - - - -

  protected static transient final String E_BAD_CC
    = "Character index %d out of range [%02d,%02d]";

  // -  Data Values and Names - - - - - - - - - - - - - - - - - - - - - - - - -

  private static transient final int[] VV_VMODE =
  {
     VZ1.COMBI_NORMAL,
     VZ1.COMBI_12    , VZ1.COMBI_34  , VZ1.COMBI_1234 , VZ1.COMBI_1_3,
     VZ1.COMBI_1_34  , VZ1.COMBI_12_3, VZ1.COMBI_12_34, VZ1.COMBI_1_2_3_4
  };
  private static transient final String[] VN_MODE =
  {
     "NORMAL",
     "1+2"   , "3+4"  , "1+2+3+4", "1/3",
     "1/3+4" , "1+2/3", "1+2/3+4", "1/2/3/4"
  };

  // -- Parameter UIDs --------------------------------------------------------

  // -  UID Parts - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

  private static final String int_UID_Cc(int c)
  {
    if (c<0 || c>11)
      throw SYX.IllArgExc(E_BAD_CC,c,0,11);
    return String.format("%02d",c+1);
  }

  // -  UIDs  - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

  /**
   * Parameter UID "{@code MODE}"
   */
  public static transient final String UID_MODE
    = "MODE";

  /**
   * Parameter UID prefix "{@code NAME}"
   */
  public static transient final String UIDpfx_NAME
    = "NAME";
  
  /**
   * Parameter UID "{@code NAME.C<cc>}"
   * 
   * @param c
   *          The zero-based character number &isin; {@code [0,11]}
   */
  public static final String UID_NAME_Cc(int c)
  {
    return UIDpfx_NAME+".C"+int_UID_Cc(c);
  }

  /**
   * Parameter UID "{@code POS_XFADE}"
   */
  public static final String UID_POSXFADE
    = "POS_XFADE";

  /**
   * Parameter UID "{@code POS_XFADE.2TONE.MIN}"
   */
  public static final String UID_POSXFADE_2TONE_MIN
    = "POS_XFADE.2TONE.MIN";

  /**
   * Parameter UID "{@code POS_XFADE.2TONE.MAX}"
   */
  public static final String UID_POSXFADE_2TONE_MAX
    = "POS_XFADE.2TONE.MAX";

  /**
   * Parameter UID "{@code POS_XFADE.4TONE.LOW.MIN}"
   */
  public static final String UID_POSXFADE_4TONE_LOW_MIN
    = "POS_XFADE.4TONE.LOW.MIN";

  /**
   * Parameter UID "{@code POS_XFADE.4TONE.LOW.MAX}"
   */
  public static final String UID_POSXFADE_4TONE_LOW_MAX
    = "POS_XFADE.4TONE.LOW.MAX";

  /**
   * Parameter UID "{@code POS_XFADE.4TONE.MID.MIN}"
   */
  public static final String UID_POSXFADE_4TONE_MID_MIN
    = "POS_XFADE.4TONE.MID.MIN";

  /**
   * Parameter UID "{@code POS_XFADE.4TONE.MID.MAX}"
   */
  public static final String UID_POSXFADE_4TONE_MID_MAX
    = "POS_XFADE.4TONE.MID.MAX";

  /**
   * Parameter UID "{@code POS_XFADE.4TONE.UP.MIN}"
   */
  public static final String UID_POSXFADE_4TONE_UP_MIN
    = "POS_XFADE.4TONE.UP.MIN";

  /**
   * Parameter UID "{@code POS_XFADE.4TONE.UP.MAX}"
   */
  public static final String UID_POSXFADE_4TONE_UP_MAX
    = "POS_XFADE.4TONE.UP.MAX";

  /**
   * Parameter UID "{@code SPLIT.1POINT}"
   */
  public static final String UID_SPLIT_1POINT
    = "SPLIT.1POINT";

  /**
   * Parameter UID "{@code SPLIT.3POINT.LOW}"
   */
  public static final String UID_SPLIT_3POINT_LOW
    = "SPLIT.3POINT.LOW";

  /**
   * Parameter UID "{@code SPLIT.3POINT.MID}"
   */
  public static final String UID_SPLIT_3POINT_MID
    = "SPLIT.3POINT.MID";

  /**
   * Parameter UID "{@code SPLIT.3POINT.UP}"
   */
  public static final String UID_SPLIT_3POINT_UP
    = "SPLIT.3POINT.UP";

  // -- Constructors and Initializers -----------------------------------------
  /**
   * Creates and zero-initializes a VZ-1/VZ-10M Operation Data model.
   * 
   * @see VZ-1/VZ-10M MIDI SysEx manual, section III-3.
   */
  public VZ1Operation()
  {
    super();
    try
    {
      SyxDataStruct prt;
      SyxParamInfo  pi;

      // -  Part 0: Mode, Assign  - - - - - - - - - - - - - - - - - - - - - - -
      // Create part
      prt = new SyxDataStruct
      (
        "0000mmmm" // Mode, key assign
      );

      // Info for parameter 'm'
      pi = new SyxParamInfo(prt,'m',UID_MODE,"Mode, Key Assign");
      pi.setValueMap(VV_VMODE,VN_MODE);
      pi.setDefaultModelValue("1+2+3+4");
      prt.addParamInfo(pi);

      // Add part
      prt.doAssertAllParamInfos();
      addParts(prt);

      // -  Part 1: Operation Name  - - - - - - - - - - - - - - - - - - - - - -
      // Create part
      prt = new SyxDataStruct
      (
        "0000aaaa 0000aaaa 0000bbbb 0000bbbb "+ // Characters  1,  2
        "0000cccc 0000cccc 0000dddd 0000dddd "+ // Characters  3,  4
        "0000eeee 0000eeee 0000ffff 0000ffff "+ // Characters  5,  6
        "0000gggg 0000gggg 0000hhhh 0000hhhh "+ // Characters  7,  8
        "0000iiii 0000iiii 0000jjjj 0000jjjj "+ // Characters  9, 10
        "0000kkkk 0000kkkk 0000llll 0000llll "+ // Characters 11, 12
        "0000**** 0000**** 0000**** 0000**** ", // Not used
        "Operation name"
      );

      // Add parameter infos
      //               |0         1 |
      //               |012345678901|
      String defName = "INIT OP     ";
      for (int i=0; i<12; i++)
      {
        String descr = String.format("Character #%02d",i+1);
        pi = new SyxParamInfo(prt,(char)('a'+i),UID_NAME_Cc(i),descr);
        pi.setValueRange(0x20,0x7F,0);
        pi.setDefaultModelValue(defName.charAt(i));
        prt.addParamInfo(pi);
      }

      // Add part
      prt.doAssertAllParamInfos();
      addParts(prt);

      // -  Part 2: Pos. X-Fade & Split Point - - - - - - - - - - - - - - - - -
      // Create part
      prt = new SyxDataStruct
      (
        "00000000 000000a0 "+ // Pos. X-Fade off/On 
        "00000bbb 0000bbbb "+ // 1 Point split
        "00000ccc 0000cccc "+ // 3 Point split (low)
        "00000ddd 0000dddd "+ // 3 Point split (mid)
        "00000eee 0000eeee "+ // 3 Point split (upper)
        "00000fff 0000ffff "+ // Pos. X-Fade: 2 tone mix minimum
        "00000ggg 0000gggg "+ // Pos. X-Fade: 2 tone mix maximum
        "00000hhh 0000hhhh "+ // Pos. X-Fade: 4 tone mix low minimum
        "00000iii 0000iiii "+ // Pos. X-Fade: 4 tone mix low maximum
        "00000jjj 0000jjjj "+ // Pos. X-Fade: 4 tone mid low minimum
        "00000kkk 0000kkkk "+ // Pos. X-Fade: 4 tone mid low maximum
        "00000lll 0000llll "+ // Pos. X-Fade: 4 tone upper low minimum
        "00000mmm 0000mmmm "+ // Pos. X-Fade: 4 tone upper low maximum
        "Pos. X-Fade & Split Point"
      );

      // TODO: Add parameter infos

      // Add part
      prt.doAssertAllParamInfos();
      addParts(prt);

      // -  Parts 3...42: Sound Data (4x) - - - - - - - - - - - - - - - - - - -
      for (int sd=0; sd<4; sd++)
      {
        // -  TODO: Part 3+10*sd: Voice No. - - - - - - - - - - - - - - - - - - - - -

        // -  TODO: Part 4+10*sd: Solo, Sus. Pedal, Vel./Vib./Trem. Inv.  - - - - - -

        // -  TODO: Part 5+10*sd: Portamento & Pitch Bend - - - - - - - - - - - - - -

        // -  TODO: Part (6-9)+10*sd: After Touch ~ Foot VR - - - - - - - - - - - - -

        // -  TODO: Part 10+10*sd: Level  - - - - - - - - - - - - - - - - - - - - - -

        // -  TODO: Part 11+10*sd: Combi Pitch  - - - - - - - - - - - - - - - - - - -

        // -  TODO: Part 12+10*sd: Vel. Split & Delay Trig. - - - - - - - - - - - - -

      }

      // -- Initialize patch data ---------------------------------------------
      reset();
    }
    catch (Throwable e)
    {// Should not happen
      throw SYX.InternalError(e);
    }
  }

  /**
   * Creates a VZ-1/VZ-10M Operation Data model and initializes it from a byte
   * array.
   * 
   * <p><b>Note:</b> The constructor does neither validate nor initialize the 
   * checksum. Invoke {@link #validateChecksum()} to validate the checksum in 
   * {@code data} or {@link #updateChecksum()} to update the checksum in this
   * model!</p>
   * 
   * @param data
   *          A byte array containing VZ Operation Data, including the checksum.
   *          The array <em>must not</em> begin with a system exclusive status
   *          byte (0xF0 or 0xF7). A tailing end-of-exclusive byte (EOX, 0xF7)
   *          will be ignored. If initializing with data from a {@link
   *          SyxMessage} message, use {@code data=}{@link
   *          SyxMessage#getData()}!
   * @throws InvalidMidiDataException
   *          if {@code data} contains ill-formatted or incompatible data 
   * @see VZ-1/VZ-10M MIDI SysEx manual, section III-3. 
   */
  public VZ1Operation(byte[] data)
  throws InvalidMidiDataException
  {
    this();
    if (data==null)
      throw new InvalidMidiDataException("Argument 'data' is null");
    setMessage(0xF0,data,data.length);
  }

  /**
   * Initializes this model with default values.
   * 
   * @see VZ-1 User Manual, p. 99
   */
  @Override
  public void reset()
  {
    super.reset();
  }

  // -- API: Getters and Setters ----------------------------------------------

  @Override
  public String getName()
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void setName(String name)
  {
    // TODO Auto-generated method stub
  }

  @Override
  public int getNumberOfParts()
  {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public String getPatchBankUID(int part) throws IllegalArgumentException
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getPatchUID(int part) throws IllegalArgumentException
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void setPatch(int part, String bankUID, String patchUID)
  throws IllegalArgumentException
  {
    // TODO Auto-generated method stub
  }

  @Override
  public boolean supportsSetPartActive()
  {
    return false;
  }

  @Override
  public boolean isPartActive(int part)
  throws IllegalArgumentException
  {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean supportsSetMidiChannel()
  {
    return false;
  }

  @Override
  public int getMidiChannel(int part)
  throws IllegalArgumentException
  {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public boolean supportsPolyphony()
  {
    return false;
  }

  @Override
  public int getPolyphony(int part)
  throws IllegalArgumentException
  {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public boolean supportsReserveNotes()
  {
    return false;
  }

  @Override
  public float getVolume(int part)
  throws IllegalArgumentException
  {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void setVolume(int part, float volume)
  throws IllegalArgumentException
  {
    // TODO Auto-generated method stub
  }

  @Override
  public float getDetune(int part)
  throws IllegalArgumentException
  {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void setDetune(int part, float detune)
  throws IllegalArgumentException
  {
    // TODO Auto-generated method stub
  }

  @Override
  public boolean supportsPan()
  {
    return false;
  }

  @Override
  public float getPan(int part)
  throws IllegalArgumentException
  {
    // TODO Auto-generated method stub
    return 0;
  }

  // -- Pretty Printing -------------------------------------------------------

}

// EOF