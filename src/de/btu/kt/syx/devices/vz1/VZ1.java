package de.btu.kt.syx.devices.vz1;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.SysexMessage;

import de.btu.kt.syx.SYX;
import de.btu.kt.syx.devices.AInstrument;
import de.btu.kt.syx.midi.SyxDataStruct;
import de.btu.kt.syx.midi.SyxMessage;
import de.btu.kt.syx.util.SysexRecorder;

/**
 * Casio VZ-1/VZ-10M device model.
 *
 * @author Matthias Wolff
 */
public class VZ1 extends AInstrument 
{
  private static final long serialVersionUID = 1L;

  // -  Constants -- Error Messages - - - - - - - - - - - - - - - - - - - - - -

  public static transient final String E_BAD_CC
    = "Character index %d out of range [%02d,%02d]";

  public static transient final String E_BAD_M
    = "Module number %d out of range [%d,%d]";

  public static transient final String E_BAD_L
    = "Line number %d out of range [%d,%d]";

  public static transient final String E_BAD_Ls
    = "Line ID %c out of range [%c,%c]";

  public static transient final String E_BAD_P
    = "Key-following point %d out of range [%d,%d]";

  public static transient final String E_BAD_S
    = "Envelope step %d out of range [%d,%d]";

  // -- Constants -- SysEx General --------------------------------------------
  
  /**
   * VZ SysEx header format.
   * 
   * <p><b>Parameters:</b><table style="margin-left:4em">
   *   <tr><td><b>{@code 'c'}</b>
   *     Casio device type in { {@link #DT_CZ}, {@link #DT_VZ} }
   *   </td></tr>
   *   <tr><td><b>{@code '#'}</b>
   *     Device number in [0,15]
   *   </td></tr>
   *   <tr><td><b>{@code 't'}</b>
   *     Bulk type, one of
   *     <table style="margin-left:2em">
   *       <tr><td>&bullet;
   *         {@link #BT_TONE}, parameter {@code 'd'} specifies the target memory
   *         area in { {@link #TM_NORMAL}, {@link #TM_COMBI1}, ..., {@link 
   *         #TM_COMBI4} },
   *       </td></tr>
   *       <tr><td>&bullet;
   *         {@link #BT_OPERATION}, parameter {@code 'd'} is {@code 0x40},
   *       </td></tr>
   *       <tr><td>&bullet;
   *         {@link #BT_MULTI}, parameter {@code 'd'} is {@code 0x00}, or 
   *       </td></tr>
   *       <tr><td>&bullet;
   *         {@link #BT_SAVE_DATA}, parameter {@code 'd'} is {@code 0x74} 
   *       </td></tr>
   *     </table>
   *   </td></tr>
   *   <tr><td><b>{@code 'd'}</b>
   *     Bulk data type, see parameter {@code 't'}
   *   </td></tr>
   * </table></p>
   */
  public static transient final String F_SYSEX_HEADER
    = "44H 000000cc 00H 7#H 0tH ddH";

  /**
   * Casio CZ device type.
   */
  public static transient final int DT_CZ = 0x00;

  /**
   * Casio VZ device type.
   */
  public static transient final int DT_VZ = 0x03;

  /**
   * {@linkplain VZ1Patch Tone Data} bulk type.
   */
  public static transient final int BT_TONE = 0x00;

  /**
   * Operation Data bulk type.
   */
  public static transient final int BT_OPERATION = 0x01;

  /**
   * Operation Data bulk type.
   */
  public static transient final int BT_MULTI = 0x02;

  /**
   * Save data bulk type (heavy bulk).
   */
  public static transient final int BT_SAVE_DATA = 0x74;

  /**
   * Normal mode {@linkplain VZ1Patch Tone Data} target memory
   */
  public static transient final int TM_NORMAL = 0x40;

  /**
   * Combination voice 1 {@linkplain VZ1Patch Tone Data} target memory
   */
  public static transient final int TM_COMBI1 = 0x41;

  /**
   * Combination voice 2 {@linkplain VZ1Patch Tone Data} target memory
   */
  public static transient final int TM_COMBI2 = 0x42;

  /**
   * Combination voice 3 {@linkplain VZ1Patch Tone Data} target memory
   */
  public static transient final int TM_COMBI3 = 0x43;

  /**
   * Combination voice 4 {@linkplain VZ1Patch Tone Data} target memory
   */
  public static transient final int TM_COMBI4 = 0x44;

  // -  Constants -- Data Values and Names  - - - - - - - - - - - - - - - - - -

  public static transient final int[]    VV_OFFON   = {      0,         1};
  public static transient final String[] VN_OFFON   = {  "OFF",      "ON"};
  public static transient final int[]    VV_ONOFF   = {      0,         1};
  public static transient final String[] VN_ONOFF   = {   "ON",     "OFF"};
  public static transient final int[]    VV_LNALG   = {VZ1.MIX, VZ1.PHASE, VZ1.RING};
  public static transient final String[] VN_LNALG   = {"MIX"  , "PHASE"  , "RING"  };
  public static transient final int[]    VV_WFORM   =
  {
    VZ1.SINE, VZ1.SAW1, VZ1.SAW2, VZ1.SAW3, VZ1.SAW4, VZ1.SAW5,
    VZ1.NOISE1, VZ1.NOISE2
  };
  public static transient final String[] VN_WFORM   =
  {
    "SINE", "SAW1", "SAW2", "SAW3", "SAW4", "SAW5",
    "NOISE1", "NOISE2"
  };
  public static transient final int[]    VV_DETRNG  = {         0,       1};
  public static transient final String[] VN_DETRNG  = {      "x1", "x1/16"};
  public static transient final int[]    VV_POL     = {         0,       1};
  public static transient final String[] VN_POL     = {       "-",     "+"};
  public static transient final int[]    VV_VELRATE = {         0,       1};
  public static transient final String[] VN_VELRATE = {       "*",     "E"};
  public static transient final int[]    VV_SUSTAIN = {         0,       1};
  public static transient final String[] VN_SUSTAIN = {      "NO",   "SUS"};
  public static transient final int[]    VV_PENVRNG = {VZ1.NARROW,VZ1.WIDE};
  public static transient final String[] VN_PENVRNG = {  "NARROW",  "WIDE"};
  public static transient final int[]    VV_VSCURVE = 
  {
    VZ1.C1, VZ1.C2, VZ1.C3, VZ1.C4, VZ1.C5, VZ1.C6, VZ1.C7, VZ1.C8
  };
  public static transient final int[]    VN_VSCURVE = SYX.getIntRange(1,8);
  public static transient final int[]    VV_VTWAVE  =
  {
     VZ1.TRIANG, VZ1.SAWUP, VZ1.SAWDN, VZ1.SQUARE
  };
  public static transient final String[] VN_VTWAVE  =
  {
     "TRIANGLE", "SAW_UP", "SAW_DOWN", "SQUARE"
  };

  // -  MIDI <-> Model Value Maps - - - - - - - - - - - - - - - - - - - - - - -

  // Note 2)

  /**
   * Permissible MIDI values according to Tone Data Note 2) in the VZ-1/VZ-10M
   * MIDI System Exclusive Specification.
   *
   * @see #note2_modelValues()
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 16
   */
  public static final int[] note2_midiValues()
  {
    return new int[]
      {
        //*0    *1    *2    *3    *4    *5    *6    *7    *8    *9
        0x00, 0x01, 0x02, 0x03, 0x05, 0x06, 0x07, 0x08, 0x0A, 0x0B, // 0*
        0x0C, 0x0E, 0x0F, 0x10, 0x11, 0x13, 0x14, 0x15, 0x17, 0x18, // 1*
        0x19, 0x1A, 0x1C, 0x1D, 0x1E, 0x20, 0x21, 0x22, 0x23, 0x25, // 2*
        0x26, 0x27, 0x29, 0x2A, 0x2B, 0x2C, 0x2E, 0x2F, 0x30, 0x32, // 3*
        0x33, 0x34, 0x35, 0x37, 0x38, 0x39, 0x3B, 0x3C, 0x3D, 0x3E, // 4*
        0x40, 0x41, 0x42, 0x43, 0x45, 0x46, 0x47, 0x49, 0x4A, 0x4B, // 5*
        0x4C, 0x4E, 0x4F, 0x50, 0x52, 0x53, 0x54, 0x55, 0x57, 0x58, // 6*
        0x59, 0x5B, 0x5C, 0x5D, 0x5E, 0x60, 0x61, 0x62, 0x64, 0x65, // 7*
        0x66, 0x67, 0x69, 0x6A, 0x6B, 0x6D, 0x6E, 0x6F, 0x70, 0x72, // 8*
        0x73, 0x74, 0x76, 0x77, 0x78, 0x79, 0x7B, 0x7C, 0x7D, 0x7F, // 9*
      };
  }

  /**
   * Permissible model values according to Tone Data Note 2) in the VZ-1/VZ-10M
   * MIDI System Exclusive Specification.
   *
   * @see #note2_midiValues()
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 16
   */
  public static final int[] note2_modelValues()
  {
    return SYX.getIntRange(0,99);
  }

  // Note 3)

  /**
   * Permissible MIDI values according to Tone Data Note 2) in the VZ-1/VZ-10M
   * MIDI System Exclusive Specification.
   *
   * @see #note2_modelValues()
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 16
   */
  public static final int[] note3_midiValues()
  {
    int[] midiValues = SYX.getIntRange(0x1C,0x7F);
    midiValues[0] = 0x00;
    return midiValues;
  }

  /**
   * Permissible model values according to Tone Data Note 2) in the VZ-1/VZ-10M
   * MIDI System Exclusive Specification.
   *
   * @see #note2_midiValues()
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 16
   */
  public static final int[] note3_modelValues()
  {
    return SYX.getIntRange(0,99);
  }

  // Note 4)

  /**
   * The smallest permissible MIDI value according to Tone Data Note 4) in the
   * VZ-1/VZ-10M MIDI System Exclusive Specification.
   * 
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 17
   * @see #note4_maxMidiValue
   * @see #note4_modelValueOffset
   */
  public static transient final int note4_minMidiValue = 0x01;

  /**
   * The largest permissible MIDI value according to Tone Data Note 4) in the
   * VZ-1/VZ-10M MIDI System Exclusive Specification.
   * 
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 17
   * @see #note4_minMidiValue
   * @see #note4_modelValueOffset
   */
  public static transient final int note4_maxMidiValue = 0x7F;

  /**
   * The model value offset according to Tone Data Note 4) in the VZ-1/VZ-10M
   * MIDI System Exclusive Specification.
   * 
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 17
   * @see #note4_minMidiValue
   * @see #note4_maxMidiValue
   */
  public static transient final int note4_modelValueOffset = -0x40;

  // Note 5)

  /**
   * Permissible MIDI values according to Tone Data Note 5) in the VZ-1/VZ-10M
   * MIDI System Exclusive Specification.
   *
   * @see #note5_modelValues()
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 17
   */
  public static final int[] note5_midiValues()
  {
    int[] midiValues = SYX.getIntRange(0x00,0x63);
    midiValues[0x63] = 0x7F;
    return midiValues;
  }

  /**
   * Permissible model values according to Tone Data Note 5) in the VZ-1/VZ-10M
   * MIDI System Exclusive Specification.
   *
   * @see #note5_midiValues()
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 17
   */
  public static final int[] note5_modelValues()
  {
    int modelValues[] = new int[100];
    for (int i=0; i<100; i++)
      modelValues[i]=99-i;
    return modelValues;
  }

  // Note 6)

  /**
   * Permissible MIDI values according to Tone Data Note 6) in the VZ-1/VZ-10M
   * MIDI System Exclusive Specification.
   *
   * @see #note6_modelValues()
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 18
   */
  public static final int[] note6_midiValues()
  {
    return SYX.getIntRange(0x00,0x3F);
  }

  /**
   * Permissible model values according to Tone Data Note 6) in the VZ-1/VZ-10M
   * MIDI System Exclusive Specification.
   *
   * @see #note6_midiValues()
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 18
   */
  public static final int[] note6_modelValues()
  {
    int modelValues[] = new int[64];
    for (int i=0; i<64; i++)
      modelValues[i]=63-i;
    return modelValues;
  }

  // Note 7)

  /**
   * Permissible MIDI values according to Tone Data Note 7) in the VZ-1/VZ-10M 
   * MIDI System Exclusive Specification.
   * 
   * @param point
   *         The support point in {{@link VZ1#P1}, ..., {@link VZ1#P6}}
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 18
   */
  public static final int[] note7_midiValues(int point)
  throws IllegalArgumentException
  {
    switch (point)
    {
    case VZ1.P1: return SYX.getIntRange(0x0C,0x73);
    case VZ1.P2: return SYX.getIntRange(0x0D,0x74);
    case VZ1.P3: return SYX.getIntRange(0x0E,0x75);
    case VZ1.P4: return SYX.getIntRange(0x0F,0x76);
    case VZ1.P5: return SYX.getIntRange(0x10,0x77);
    case VZ1.P6: return SYX.getIntRange(0x11,0x78);
    default:
      throw SYX.IllArgExc(E_BAD_P,point,VZ1.P1,VZ1.P6);
    }
  }

  /**
   * Permissible model value names according to Tone Data Note 7) in the
   * VZ-1/VZ-10M MIDI System Exclusive Specification.
   *
   * @param point
   *         The support point in {{@link VZ1#P1}, ..., {@link VZ1#P6}}
   * @see #note7_midiValues()
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 18
   */
  public static final String[] note7_valueNames(int point)
  {
    if (point<VZ1.P1 || point>VZ1.P6)
      throw SYX.IllArgExc(E_BAD_P,point,VZ1.P1,VZ1.P6);

    return int_note789_valueNames(note7_midiValues(point));
  }

  /**
   * Permissible model value names according to Notes 7)...9) in the VZ-1/VZ-10M
   * MIDI System Exclusive Specification.
   *
   * @param midiValues
   *         Array of permissible MIDI values
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 18+23
   */
  private static final String[] int_note789_valueNames(int[] midiValues)
  {
    ArrayList<String> valueNames = new ArrayList<String>();
    String[] noteNames = new String[]
      {
        "C", "C#", "D", "Eb", "E", "F", "F#", "G", "Ab", "A", "Bb", "B"
      };
    for (int midiValue : midiValues)
    {
      int oct  = (midiValue-0x0C) / 12;
      int note = (midiValue-0x0C) % 12;
      valueNames.add(String.format("%s%d",noteNames[note],oct));
    }
    return valueNames.toArray(new String[0]);
  }

  // TODO: Note 8)

  // TODO: Note 9)

  // -- Constants -- VZ Tone Data (Patches) -----------------------------------

  // -  Lines - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

  /**
   * Line A (modules M1, M2)
   */
  public static transient final int A = 0;

  /**
   * Line B (modules M3, M4)
   */
  public static transient final int B = 1;

  /**
   * Line C (modules M5, M6)
   */
  public static transient final int C = 2;

  /**
   * Line D (modules M7, M8)
   */
  public static transient final int D = 3;

  // -  Modules - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 

  /**
   * Module M1
   */
  public static transient final int M1 = 0;

  /**
   * Module M2
   */
  public static transient final int M2 = 1;

  /**
   * Module M3
   */
  public static transient final int M3 = 2;

  /**
   * Module M4
   */
  public static transient final int M4 = 3;

  /**
   * Module M5
   */
  public static transient final int M5 = 4;

  /**
   * Module M6
   */
  public static transient final int M6 = 5;

  /**
   * Module M7
   */
  public static transient final int M7 = 6;

  /**
   * Module M8
   */
  public static transient final int M8 = 7;

  // -  Line Algorithms - - - - - - - - - - - - - - - - - - - - - - - - - - - -

  /**
   * Line algorithm {@code MIX} (mixed waveform output). 
   * 
   * <p style="margin-left:2em">
   *   <b>{@code M<n>}&hairsp;+&hairsp;{@code M<n>}</b>
   *   &emsp;
   *   with {@code <n>}&hairsp;&isin;&hairsp;{1,3,5,7}
   *   and  {@code <m>}&hairsp;&isin;&hairsp;{2,4,6,8}
   * </p>
   */
  public static transient final int MIX = 0;

  /**
   * Line algorithm {@code PHASE} (phase distortion modulation). 
   * 
   * <p style="margin-left:2em">
   *   <b>{@code M<m>(M<n>)}</b>
   *   &emsp;
   *   with {@code <n>}&hairsp;&isin;&hairsp;{1,3,5,7}
   *   and  {@code <m>}&hairsp;&isin;&hairsp;{2,4,6,8}
   * </p>
   */
  public static transient final int PHASE = 1;

  /**
   * Line algorithm {@code RING} (ring modulation). 
   * 
   * <p style="margin-left:2em">
   *   <b>{@code M<m>}&hairsp;+&hairsp;{@code M<n>}&times;{@code M<m>}</b>
   *   &emsp;
   *   with {@code <n>}&hairsp;&isin;&hairsp;{1,3,5,7}
   *   and  {@code <m>}&hairsp;&isin;&hairsp;{2,4,6,8}
   * </p>
   */
  public static transient final int RING = 2;

  // -  Module Waveforms  - - - - - - - - - - - - - - - - - - - - - - - - - - -

  /**
   * Sine waveform
   */
  public static transient final int SINE = 0;

  /**
   * Saw 1 waveform
   */
  public static transient final int SAW1 = 1;

  /**
   * Saw 2 waveform
   */
  public static transient final int SAW2 = 2;

  /**
   * Saw 3 waveform
   */
  public static transient final int SAW3 = 3;

  /**
   * Saw 4 waveform
   */
  public static transient final int SAW4 = 4;

  /**
   * Saw 5 waveform
   */
  public static transient final int SAW5 = 5;

  /**
   * Noise 1 waveform
   */
  public static transient final int NOISE1 = 6;

  /**
   * Noise 2 waveform
   */
  public static transient final int NOISE2 = 7;

  // -  Module Detune Modes - - - - - - - - - - - - - - - - - - - - - - - - - -

  /**
   * Relative pitch (i.e., fix pitch off).
   */
  public static transient final int REL = 0;

  /**
   * Fix pitch, range x1
   */
  public static transient final int FIXHI = 1;

  /**
   * Fix pitch, range x1/16
   */
  public static transient final int FIXLO = 2;

  // -  Envelope Steps  - - - - - - - - - - - - - - - - - - - - - - - - - - - -

  /**
   * Envelope step 1
   */
  public static transient final int S1 = 0;

  /**
   * Envelope step 2
   */
  public static transient final int S2 = 1;

  /**
   * Envelope step 3
   */
  public static transient final int S3 = 2;

  /**
   * Envelope step 4
   */
  public static transient final int S4 = 3;

  /**
   * Envelope step 5
   */
  public static transient final int S5 = 4;

  /**
   * Envelope step 6
   */
  public static transient final int S6 = 5;

  /**
   * Envelope step 7
   */
  public static transient final int S7 = 6;

  /**
   * Envelope step 8
   */
  public static transient final int S8 = 7;

  // -  Key Following Support Points  - - - - - - - - - - - - - - - - - - - - -

  /**
   * Key following support point 1
   */
  public static transient final int P1 = 0;

  /**
   * Key following support point 2
   */
  public static transient final int P2 = 1;

  /**
   * Key following support point 3
   */
  public static transient final int P3 = 2;

  /**
   * Key following support point 4
   */
  public static transient final int P4 = 3;

  /**
   * Key following support point 5
   */
  public static transient final int P5 = 4;

  /**
   * Key following support point 6
   */
  public static transient final int P6 = 5;

  // -  Velocity Sensitivity Curves - - - - - - - - - - - - - - - - - - - - - -

  /**
   * Velocity sensitivity curve 1
   */
  public static transient final int C1 = 0;

  /**
   * Velocity sensitivity curve 2
   */
  public static transient final int C2 = 1;

  /**
   * Velocity sensitivity curve 3
   */
  public static transient final int C3 = 2;

  /**
   * Velocity sensitivity curve 4
   */
  public static transient final int C4 = 3;

  /**
   * Velocity sensitivity curve 5
   */
  public static transient final int C5 = 4;

  /**
   * Velocity sensitivity curve 6
   */
  public static transient final int C6 = 5;

  /**
   * Velocity sensitivity curve 7
   */
  public static transient final int C7 = 6;

  /**
   * Velocity sensitivity curve 8
   */
  public static transient final int C8 = 7;

  // -  Vibrato/Tremolo Waveforms - - - - - - - - - - - - - - - - - - - - - - -

  /**
   * Triangular vibrato or tremolo waveform
   */
  public static transient final int TRIANG = 0;

  /**
   * Rising sawtooth vibrato or tremolo waveform
   */
  public static transient final int SAWUP = 1;

  /**
   * Falling sawtooth vibrato or tremolo waveform
   */
  public static transient final int SAWDN = 2;

  /**
   * Rectangular vibrato or tremolo waveform
   */
  public static transient final int SQUARE = 3;

  // -  Pitch Envelope Ranges - - - - - - - - - - - - - - - - - - - - - - - - -
  
  /**
   * Narrow pitch envelope range
   */
  public static transient final int NARROW = 0;
  
  /**
   * Wide pitch envelope range
   */
  public static transient final int WIDE = 1;

  // -- Constants -- VZ Operation Data  ---------------------------------------

  // -  Mode  - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

  /**
   * Combination mode "NORMAL"
   */
  public static transient final int COMBI_NORMAL = 0x00;

  /**
   * Combination mode "1+2"
   */
  public static transient final int COMBI_12 = 0x01;

  /**
   * Combination mode "3+4"
   */
  public static transient final int COMBI_34 = 0x02;

  /**
   * Combination mode "1+2+3+4"
   */
  public static transient final int COMBI_1234 = 0x03;

  /**
   * Combination mode "1/3"
   */
  public static transient final int COMBI_1_3 = 0x04;

  /**
   * Combination mode "1/3+4"
   */
  public static transient final int COMBI_1_34 = 0x05;

  /**
   * Combination mode "1+2/3"
   */
  public static transient final int COMBI_12_3 = 0x06;

  /**
   * Combination mode "1+2/3+4"
   */
  public static transient final int COMBI_12_34 = 0x07;

  /**
   * Combination mode "1_2_3_4"
   */
  public static transient final int COMBI_1_2_3_4 = 0x08;

  // -- Attributes ------------------------------------------------------------

  /**
   * VZ {@linkplain VZ1Patch tone data} buffer.
   */
  protected VZ1Patch patch = null;

  // -- Constructors ----------------------------------------------------------

  /**
   * Creates a new VZ-1/VZ-10M adapter instance. 
   * 
   * @param devNum
   *          The {@linkplain AInstrument#devNum device number} of the 
   *          instrument (1, ...,{@link AInstrument#getMaxDeviceNumber 
   *          getMaxDeviceNumber}{@code ()})
   * @param verbose
   *          Verbose level for console log, 0: silence
   * @throws IllegalArgumentException
   *          if {@code mi} is {@code null}, or if {@code devNum} is out of range
   */
  public VZ1(int devNum, int verbose) 
  throws IllegalArgumentException 
  {
    super(devNum,verbose);
  }

  /**
   * Creates a new VZ-1/VZ-10M adapter instance. 
   * 
   * @param devNum
   *          The {@linkplain AInstrument#devNum device number} of the 
   *          instrument (1, ...,{@link AInstrument#getMaxDeviceNumber 
   *          getMaxDeviceNumber}{@code ()})
   * @throws IllegalArgumentException
   *          if {@code mi} is {@code null}, or if {@code devNum} is out of range
   */
  public VZ1(int devNum) 
  throws IllegalArgumentException 
  {
    this(devNum,0);
  }

  // -- API: Getters and Setters ----------------------------------------------

  /**
   * Returns a reference the {@linkplain VZ1Patch tone data} buffer of this VZ 
   * adapter instance. Changes made in this object will modify the adapter's 
   * state.
   */
  public VZ1Patch getPatch()
  {
    return patch;
  }

  /**
   * Sets the {@linkplain VZ1Patch tone data} buffer.
   * 
   * @param patch
   *          The new tone data.
   */
  public void setPatch(VZ1Patch patch)
  {
    this.patch = patch;
  }

  // -- API: Send SysEx Messages t0 VZ ----------------------------------------

  /**
   * Sends the current Tone Data buffer to VZ.
   *
   * @param target Target buffer in VZ, one of
   *               <table>
   *                 <tr>
   *                   <td>&bullet; {@link #TM_NORMAL}:</td>
   *                   <td>Normal mode working area, or</td>
   *                 </tr>
   *                 <tr>
   *                   <td>
   *                     &bullet; {@link #TM_COMBI1}, ..., {@link #TM_COMBI4}:
   *                   </td>
   *                   <td>Combi mode, patch 1...4 working area.</td>
   *                 </tr>
   *               </table>
   * @throws IllegalArgumentException if the argument is out of range
   * @throws MidiUnavailableException if the sending the SysEx bulk failed
   * @see VZ-1 User Manual,  pp. 81&ndash;83
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, pp. 1&ndash;2
   */
  public void sendPatch(int target)
  throws IllegalArgumentException, MidiUnavailableException
  {
    if (!SYX.valueIn(target,VZ1.TM_NORMAL,VZ1.TM_COMBI4))
      throw SYX.IllArgExc("Argument 'target'=%d out of range",target);

    patch.updateChecksum();                                                    // Update Tone Data checksum
    try                                                                        // Try
    {                                                                          // >>
      SyxDataStruct sxdh = new SyxDataStruct(F_SYSEX_HEADER);                  //   Create bulk header struct. and set
      sxdh.setMidiValue('#',devNum-1);                                         //   ... Device number
      sxdh.setMidiValue('c',DT_VZ);                                            //   ... Casio device type
      sxdh.setMidiValue('t',BT_TONE);                                          //   ... Bulk type
      sxdh.setMidiValue('d',target  );                                         //   ... Target working area
      SyxMessage sxm = new SyxMessage();                                       //   Create SysEx message and append
      sxm.addParts(sxdh);                                                      //   ... Bulk header
      sxm.addParts(patch.getParts());                                          //   ... Tone Data buffer
      sendSysexMsg(sxm);                                                       //   Send SysEx message
    }                                                                          // <<
    catch (NullPointerException e)                                             // Catch NullPointerException
    { // No MIDI device attached to adapter                                    // <<
      MidiUnavailableException e2                                              //   Create wrapping exception
        = new MidiUnavailableException("No MIDI device attached");             //   ...
      e2.initCause(e);                                                         //   ...
      throw e2;                                                                //   Throw it
    }                                                                          // <<
    catch (InvalidMidiDataException e)                                         // Catch NullPointerException
    { // Cannot happen                                                         // >>
      throw new Error("Internal error",e);                                     //   Throw error (likely a bug!)
    }                                                                          // <<
  }

  // -- API: Static Utilities -------------------------------------------------

  /**
   * Converts the index of a bank element (patch or operation) to its identifier
   * as seen on VZ display.
   * 
   * @param index
   *          The zero-based bank element index in [0,63]
   * @return The canonical identifier in {"{@code A-1}", "{@code A-2}", ...,
   *          "{@code H-8}"}
   * @throws IllegalArgumentException
   *          if the argument is out of range
   */
  public static String bankElemIndex2Id(int index)
  throws IllegalArgumentException
  {
    if (!SYX.valueIn(index,0,63))
      throw SYX.IllArgExc("Argument 'index'=%d out of range",index);
    return String.valueOf((char)(index/8+'A'))+"-"+(index%8+1);
  }

  /**
   * Converts an identifier of a bank element (patch or operation) to its index.
   * 
   * @param id
   *          The identifier in {"{@code A-1}", "{@code A-2}", ..., "{@code
   *          H-8}"}. The method leniently accepts
   *          <ul>
   *            <li>identifiers with lower-case letters '{@code a}'...'{@code
   *              h}', and </li>
   *            <li>identifiers without hyphen, e.g., "{@code A1}".</li>
   *          </ul>
   * @return The zero-based bank element index in [0,63]
   * @throws IllegalArgumentException
   *          if the argument is not a valid bank element identifier, more
   *          precisely: if the argument does not match the regular expression
   *          "{@code [a-hA-H]-?[1-8]}"
   */
  public static int bankElemId2Index(String id)
  throws IllegalArgumentException
  {
    if (id==null ||id.length()==0)
      throw SYX.IllArgExc("Argument must not be null or empty",id);
    Pattern pattern = Pattern.compile("([a-hA-H])-?([1-8])");
    Matcher matcher = pattern.matcher(id);
    if (!matcher.matches())
      throw SYX.IllArgExc("Invalid argument '%s'",id);
    int i = matcher.group(1).toUpperCase().charAt(0)-'A';
    int j = Integer.parseInt(matcher.group(2))-1;
    return i*8+j;
  }

  // -- Message Receivers -----------------------------------------------------

  /**
   * Receives a {@linkplain VZ1Patch Tone data} bulk.
   * 
   * @param msgHdr
   *          The parsed header struct of the SysEx message
   * @param sxMsg
   *          The SysEx message
   * @return {@code true} if {@code sxMsg} is a Tone data bulk, {@code false}
   *         otherwise
   */
  private boolean int_rcvToneData(SyxDataStruct msgHdr, SysexMessage sxMsg)
  {
    if (msgHdr.getMidiValue('c')!=DT_VZ  ) return false;                       // Validate Casio device type
    if (msgHdr.getMidiValue('t')!=BT_TONE) return false;                       // Validate bulk type

    log("- Tone data bulk, processing ...");                                   // Log message
    try                                                                        // Try (processing the message)
    {                                                                          // >>
      int        target    = msgHdr.getMidiValue('d');                         //   Get target memory area
      byte[]     sxMsgData = sxMsg.getMessage();                               //   Get message bytes
      VZ1Patch   tdTemp    = new VZ1Patch();                                   //   Create temporary VZ1Patch
      SyxMessage sxBulk    = new SyxMessage();                                 //   Create VZ bulk message model
      sxBulk.addParts(new SyxDataStruct(F_SYSEX_HEADER,"Bulk Header"));        //   ... Add bulk header structure
      sxBulk.addParts(tdTemp.getParts());                                      //   ... Add VZ1Patch structure
      sxBulk.setMessage(sxMsgData,sxMsgData.length);                           //   Set and parse message bytes
      tdTemp.validateChecksum();                                               //   Validate received checksum
      patch = tdTemp;                                                          //   Set internal Tone Data buffer
      log(" ok\n- Target memory area: 0x%02X (ignored)\n",target);             //   Log message
      log(2,2,"");
      log(2,patch.prettyPrintModel());                                         //   Pretty-print new Tone Data
      log(2,-2,"");
    }                                                                          // <<
    catch (Exception e)                                                        // Catch anything
    { // Should not happen                                                     // >> (Probably due to a bug!)
      log(" FAILED\n");                                                        //   Log message
      log(e);                                                                  //   Log exception
    }                                                                          // <<
    return true;                                                               // Indicate message received
  }

  /**
   * Receives an Operation data bulk.
   * 
   * @param msgHdr
   *          The parsed header struct of the SysEx message
   * @param sxMsg
   *          The SysEx message
   * @return {@code true} if {@code sxMsg} is an Operation data bulk, {@code 
   *         false} otherwise
   */
  private boolean int_rcvOpData(SyxDataStruct msgHdr, SysexMessage sxMsg)
  {
    if (msgHdr.getMidiValue('c')!=DT_VZ       ) return false;                  // Validate Casio device type
    if (msgHdr.getMidiValue('t')!=BT_OPERATION) return false;                  // Validate bulk type
    log("- Operation data bulk, NOT YET IMPLENTED");                           // Log message
    return true;                                                               // Indicate message received
  }

  /**
   * Receives a Multi data bulk.
   * 
   * @param msgHdr
   *          The parsed header struct of the SysEx message
   * @param sxMsg
   *          The SysEx message
   * @return {@code true} if {@code sxMsg} is a Multi data bulk, {@code 
   *         false} otherwise
   */
  private boolean int_rcvMultiData(SyxDataStruct msgHdr, SysexMessage sxMsg)
  {
    if (msgHdr.getMidiValue('c')!=DT_VZ   ) return false;                      // Validate Casio device type
    if (msgHdr.getMidiValue('t')!=BT_MULTI) return false;                      // Validate bulk type
    log("- Multi data bulk, NOT YET IMPLENTED");                               // Log message
    return true;                                                               // Indicate message received
  }

  /**
   * Receives a heavy data bulk. A "heavy" data bulk contains a complete patch
   * and/or operation bank.
   * 
   * @param msgHdr
   *          The parsed header struct of the SysEx message
   * @param sxMsg
   *          The SysEx message
   * @return {@code true} if {@code sxMsg} is a heavy data bulk, {@code 
   *         false} otherwise
   */
  private boolean int_rcvSavedData(SyxDataStruct msgHdr, SysexMessage sxMsg)
  {
    if (msgHdr.getMidiValue('c')!=DT_VZ       ) return false;                  // Validate Casio device type
    if (msgHdr.getMidiValue('t')!=BT_SAVE_DATA) return false;                  // Validate bulk type
    log("- Heavy data bulk, NOT YET IMPLENTED");                               // Log message
    return true;                                                               // Indicate message received
  }

  /**
   * Receives a SysEx command.
   * 
   * @param msgHdr
   *          The parsed header struct of the SysEx message
   * @param sxMsg
   *          The SysEx message
   * @return {@code true} if {@code sxMsg} is a SysEx command, {@code false}
   * otherwise
   */
  private boolean int_rcvSyxCmd(SyxDataStruct msgHdr, SysexMessage sxMsg)
  {
    log("- Other SysEx message, NOT YET IMPLENTED");                           // Log message
    return true;                                                               // Indicate message received
  }

  // -- Implementation of AInstrument -----------------------------------------

  @Override
  public void processSysexMsg(SysexMessage sxMsg) 
  throws InvalidMidiDataException 
  {
    // Accept or Reject Incoming SysEx Message                                 // - - - - - - - - - - - - - - - - - - -
    SyxDataStruct msgHdr = new SyxDataStruct(F_SYSEX_HEADER,sxMsg.getData());  // Parse message header
    if (msgHdr.getMidiValue('#')!=devNum-1)                                    // Not for this adapter's device number
      throw new InvalidMidiDataException();                                    // -> Indicate message not processed
    log("SysEx message received\n");                                           // Log message
    if (getVerbose()>=3)                                                       // Pretty printing takes long >>
      log(3,SYX.prettyPrintMidiMessage(sxMsg)+"\n");                           //   Pretty-print received SysEx message

    // Process VZ SysEx Message                                                // - - - - - - - - - - - - - - - - - - -
    try                                                                        // Try processing VZ SysEx message
    {                                                                          // >>
      if (int_rcvToneData (msgHdr,sxMsg)) return;                              //   Try receiving Tone data bulk
      if (int_rcvOpData   (msgHdr,sxMsg)) return;                              //   Try receiving Operation data bulk
      if (int_rcvMultiData(msgHdr,sxMsg)) return;                              //   Try receiving Multi data bulk
      if (int_rcvSavedData(msgHdr,sxMsg)) return;                              //   Try receiving a heavy data bulk
      if (int_rcvSyxCmd   (msgHdr,sxMsg)) return;                              //   Try receiving SysEx command
      throw new Exception("Unknown message type");                             //   None of these (should not happen)
    }                                                                          // <<
    catch (Exception e)                                                        // Catch anything
    { // Should not happen                                                     // >> (Probably due to a bug!)
      log(e);                                                                  //   Log exception
    }                                                                          // <<
  }                                                                            // Indicate message processed

  @Override
  public void record(SysexRecorder recorder) 
  {
    // TODO: Method stub: record(SysexRecorder)
  }

  @Override
  protected String prettyPrint() 
  {
    // TODO: Method stub: prettyPrint()
    return null;
  }

  @Override
  protected String prettyPrintDataStructure() 
  {
    // TODO: Method stub: prettyPrintDataStructure()
    return null;
  }

  // -- Implementation of ILooger ---------------------------------------------
  
  @Override
  public String getLogID() 
  {
    return String.format("VZ-1 #%02d",this.devNum);
  }

}

// EOF