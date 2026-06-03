package de.btu.kt.syx.devices.vz1;

import javax.sound.midi.InvalidMidiDataException;

import de.btu.kt.syx.SYX;
import de.btu.kt.syx.devices.IPatch;
import de.btu.kt.syx.midi.SyxChecksum;
import de.btu.kt.syx.midi.SyxDataStruct;
import de.btu.kt.syx.midi.SyxMessage;
import de.btu.kt.syx.midi.SyxParamInfo;

/**
 * A VZ-1/VZ-10M patch (aka. "Tone Data") model. See source code for a best
 * practice example of implementing a system exclusive message model.
 *
 * <h3>Method Index</h3>
 * <table style="margin-left:1em">
 *   <tr>
 *     <th style="text-align:left">VZ Menu</th>
 *     <th style="text-align:left">Getters</th>
 *     <th style="text-align:left">Setters</th>
 *     <th style="text-align:left">Parameter UID</th>
 *   </tr>
 *   <tr>
 *     <td>(Module On/Off Selectors)</td>
 *     <td>{@link #isModuleOn(int) isModuleOn(module)}</td>
 *     <td>{@link #setModuleOn(int, boolean) setModuleOn(module,on)}</td>
 *     <td>"{@link #UID_Mm(int) M&lt;m&gt;}"
 *   </tr>
 *   <tr></tr>
 *   <tr>
 *     <td>1-00: Line</td>
 *     <td>{@link #getLineAlgorithm(int) getLineAlgorithm(line)}</td>
 *     <td>{@link #setLineAlgoritm(int, int) setLineAlgoritm(line,alg)}</td>
 *     <td>"{@link #UID_Ll_INTLINE(int) L&lt;l&gt;.INT_LINE}"</td>
 *   </tr>
 *   <tr>
 *     <td></td>
 *     <td>{@link #isExtPhase(int) isExtPhase(line)}</td>
 *     <td>{@link #setExtPhase(int, boolean) setExtPhase(line,on)}</td>
 *     <td>"{@link #UID_Le_EXTPHASE(int) L&lt;e&gt;.EXT_PHASE}"</td>
 *   </tr>
 *   <tr></tr>
 *   <tr>
 *     <td>1-01: Waveform</td>
 *     <td>{@link #getWaveform(int) getWaveform(module)}</td>
 *     <td>{@link #setWaveform(int, int) setWaveform(module,wform)}</td>
 *     <td>"{@link #UID_Mm_WAVEFORM(int) M&lt;m&gt;.WAVE_FORM}"</td>
 *   </tr>
 *   <tr></tr>
 *   <tr>
 *     <td>1-02: Detune</td>
 *     <td>{@link #getDetuneMode(int) getDetuneMode(module)}</td>
 *     <td>{@link #setDetuneRel(int, int, int, int, int) setDetuneRel(module,pol,oct,note,fine)}</td>
 *     <td>"{@link #UID_Mm_DETUNE_PITCHFIX(int) M&lt;m&gt;.DETUNE.PITCH_FIX}",
           "{@link #UID_Mm_DETUNE_RANGE(int) M&lt;m&gt;.DETUNE.RANGE}"<td>
 *   </tr>
 *   <tr>
 *     <td></td>
 *     <td>{@link #getDetunePolarity(int) getDetunePolarity(module)}</td>
 *     <td>{@link #setDetuneFix(int, int, int, int, boolean) setDetuneFix(module,oct,note,fine,low)}</td>
 *     <td>"{@link #UID_Mm_DETUNE_POL(int) M&lt;m&gt;.DETUNE.POL}"</td>
 *   </tr>
 *   <tr>
 *     <td></td>
 *     <td>{@link #getDetuneOctave(int) getDetuneOctave(module)}</td>
 *     <td></td>
 *     <td>"{@link #UID_Mm_DETUNE_OCTNOTE(int) M&lt;m&gt;.DETUNE.OCT_NOTE}"</td>
 *   </tr>
 *   <tr>
 *     <td></td>
 *     <td>{@link #getDetuneNote(int) getDetuneNote(module)}</td>
 *     <td></td>
 *     <td>"{@link #UID_Mm_DETUNE_OCTNOTE(int) M&lt;m&gt;.DETUNE.OCT_NOTE}"</td>
 *   </tr>
 *   <tr>
 *     <td></td>
 *     <td>{@link #getDetuneFine(int) getDetuneFine(module)}</td>
 *     <td></td>
 *     <td>"{@link #UID_Mm_DETUNE_FINE(int) M&lt;m&gt;.DETUNE.FINE}"</td>
 *   </tr>
 *   <tr></tr>
 *   <tr>
 *     <td>1-03: Envelope (DCO)</td>
 *     <td>{@link #getPitchEnvStepRate(int) getPitchEnvStepRate(step)}</td>
 *     <td>{@link #setPitchEnvStep(int, int, int) setPitchEnvStep(step,rate,level)}</td>
 *     <td>"{@link #UID_PITCH_ENV_Ss_RATE(int) PITCH.ENV.S&lt;s&gt;.RATE}"</td>
 *   </tr>
 *   <tr>
 *     <td></td>
 *     <td>{@link #getPitchEnvStepLevel(int) getPitchEnvStepLevel(step)}</td>
 *     <td></td>
 *     <td>"{@link #UID_PITCH_ENV_Ss_LEV(int) PITCH.ENV.S&lt;s&gt;.LEV}"</td>
 *   </tr>
 *   <tr>
 *     <td></td>
 *     <td>{@link #getPitchEnvSustainStep()}</td>
 *     <td>{@link #setPitchEnvSustainStep(int) setPitchEnvSustainStep(step)}</td>
 *     <td>"{@link #UID_PITCH_ENV_Ss_SS(int) PITCH.ENV.S&lt;s&gt;.SS}"</td>
 *   </tr>
 *   <tr>
 *     <td></td>
 *     <td>{@link #getPitchEnvEndStep()}</td>
 *     <td>{@link #setPitchEnvEndStep(step)}</td>
 *     <td>"{@link #UID_PITCH_ENV_ED PITCH.ENV.ED}"</td>
 *   </tr>
 *   <tr></tr>
 *   <tr>
 *     <td>1-04: Envelope Depth (DCO)</td>
 *     <td>{@link #getPitchEnvRange()}</td>
 *     <td>{@link #setPitchEnvRange(int) setPitchEnvRange(range)}</td>
 *     <td>"{@link #UID_PITCH_ENV_RANGE PITCH.ENV.RANGE}"</td>
 *   </tr>
 *   <tr>
 *     <td></td>
 *     <td>{@link #getPitchEnvDepth()}</td>
 *     <td>{@link #setPitchEnvDepth(int) setPitchEnvDepth(depth)}</td>
 *     <td>"{@link #UID_PITCH_ENV_DEPTH PITCH.ENV.DEPTH}"</td>
 *   </tr>
 *   <tr></tr>
 *   <tr>
 *     <td>1-05: KF Level (DCO)</td>
 *     <td>{@link #getPitchKfKey(int) getPitchKfKey(point)}</td>
 *     <td>{@link #setPitchKf(String[], int[]) setPitchKf(keys,levels)}</td>
 *     <td>"{@link #UID_PITCH_KF_Pp_KEY(int) PITCH.KF.P&lt;p&gt;.KEY}"</td>
 *   </tr>
 *   <tr>
 *     <td></td>
 *     <td>{@link #getPitchKfLevel(int) getPitchKfLevel(point)}</td>
 *     <td></td>
 *     <td>"{@link #UID_PITCH_KF_Pp_LEV(int) PITCH.KF.P&lt;p&gt;.LEV}"</td>
 *   </tr>
 *   <tr>
 *     <td></td>
 *     <td>{@link #getPitchKfKeys()}</td>
 *     <td></td>
 *   </tr>
 *   <tr>
 *     <td></td>
 *     <td>{@link #getPitchKfLevels()}</td>
 *     <td></td>
 *   </tr>
 *   <tr></tr>
 *   <tr>
 *     <td>1-06: Velocity Level (DCO)</td>
 *     <td>{@link #getPitchVelSensCurve()}</td>
 *     <td>{@link #setPitchVelSens(int, int) setPitchVelSens(curve,sens)}</td>
 *     <td>"{@link #UID_PITCH_VELLEVEL_CURVE PITCH.VEL_LEVEL.CURVE}"</td>
 *   </tr>
 *   <tr>
 *     <td></td>
 *     <td>{@link #getPitchVelSens()}</td>
 *     <td></td>
 *     <td>"{@link #UID_PITCH_VELLEVEL_SENSITIVITY PITCH.VEL_LEVEL.SENSITIVITY}"</td>
 *   </tr>
 *   <tr></tr>
 *   <tr>
 *     <td>1-07: Vibrato (DCO)</td>
 *     <td>{@link #isVibratoMulti()}</td>
 *     <td>{@link #setVibrato(boolean, int, int, int, int) setVibrato(multi,wave,depth,rate,delay)}</td>
 *     <td>"{@link #UID_VIBRATO_MULTI VIBRATO.MULTI}"</td>
 *   </tr>
 *   <tr>
 *     <td></td>
 *     <td>{@link #getVibratoWave()}</td>
 *     <td></td>
 *     <td>"{@link #UID_VIBRATO_WAVE VIBRATO.WAVE}"</td>
 *   </tr>
 *   <tr>
 *     <td></td>
 *     <td>{@link #getVibratoDepth()}</td>
 *     <td></td>
 *     <td>"{@link #UID_VIBRATO_DEPTH VIBRATO.DEPTH}"</td>
 *   </tr>
 *   <tr>
 *     <td></td>
 *     <td>{@link #getVibratoRate()}</td>
 *     <td></td>
 *     <td>"{@link #UID_VIBRATO_RATE VIBRATO.RATE}"</td>
 *   </tr>
 *   <tr>
 *     <td></td>
 *     <td>{@link #getVibratoDelay()}</td>
 *     <td></td>
 *     <td>"{@link #UID_VIBRATO_DELAY VIBRATO.DELAY}"</td>
 *   </tr>
 *   <tr></tr>
 *   <tr>
 *     <td>1-08: Octave</td>
 *     <td>{@link #getOctave()}</td>
 *     <td>{@link #setOctave(int) setOctave(value)}</td>
 *     <td>"{@link #UID_OCTAVE_POL}", "{@link #UID_OCTAVE_NUM}"</td>
 *   </tr>
 *   <tr></tr>
 *   <tr>
 *     <td>1-09: Envelope (DCA)</td>
 *     <td>{@link #getAmpEnvStepRate(int, int) getAmpEnvStepRate(module,step)}</td>
 *     <td>{@link #setAmpEnvStep(int, int, int, int) setAmpEnvStep(module,step,rate,level)}</td>
 *     <td>"{@link #UID_Mm_AMP_ENV_Ss_RATE(int, int) M&lt;m&gt;.AMP.ENV.S&lt;s&gt;.RATE}"</td>
 *   </tr>
 *   <tr>
 *     <td></td>
 *     <td>{@link #getAmpEnvStepLevel(int, int) getAmpEnvStepLevel(module,step)}</td>
 *     <td></td>
 *     <td>"{@link #UID_Mm_AMP_ENV_Ss_LEV(int, int) M&lt;m&gt;.AMP.ENV.S&lt;s&gt;.LEV}"</td>
 *   </tr>
 *   <tr>
 *     <td></td>
 *     <td>{@link #getAmpEnvSustainStep(int) getAmpEnvSustainStep(module)}&nbsp;&nbsp;</td>
 *     <td>{@link #setAmpEnvSustainStep(int, int) setAmpEnvSustainStep(module,step)}</td>
 *     <td>"{@link #UID_Mm_AMP_ENV_Ss_SS(int, int) M&lt;m&gt;.AMP.ENV.S&lt;s&gt;.SS}"</td>
 *   </tr>
 *   <tr>
 *     <td></td>
 *     <td>{@link #getAmpEnvEndStep(int) getAmpEnvEndStep(module)}</td>
 *     <td>{@link #setAmpEnvEndStep(int, int) setAmpEnvEndStep(module,step)}</td>
 *     <td>"{@link #UID_Mm_AMP_ENV_ED(int) M&lt;m&gt;.AMP.ENV.ED}"</td>
 *   </tr>
 *   <tr></tr>
 *   <tr>
 *     <td>1-10: Envelope Depth (DCA)</td>
 *     <td>{@link #getAmpEnvDepth(int) getAmpEnvDepth(module)}</td>
 *     <td>{@link #setAmpEnvDepth(int, int) setAmpEnvDepth(module,value)}</td>
 *     <td>"{@link #UID_Mm_AMP_ENV_DEPTH(int) M&lt;m&gt;.AMP.ENV.DEPTH}"</td>
 *   </tr>
 *   <tr></tr>
 *   <tr>
 *     <td>1-11: KF Level (DCA)</td>
 *     <td>{@link #getAmpKfKey(int, int) getAmpKfKey(module,point)}</td>
 *     <td>{@link #setAmpKf(int, String[], int[]) setAmpKf(module,keys,levels)}</td>
 *     <td>"{@link #UID_Mm_AMP_KF_Pp_KEY(int, int) M&lt;m&gt;.AMP.KF.P&lt;p&gt;.KEY}"</td>
 *   </tr>
 *   <tr>
 *     <td></td>
 *     <td>{@link #getAmpKfLevel(int, int) getAmpKfLevel(module,point)}</td>
 *     <td></td>
 *     <td>"{@link #UID_Mm_AMP_KF_Pp_LEV(int, int) M&lt;m&gt;.AMP.KF.P&lt;p&gt;.LEV}"</td>
 *   </tr>
 *   <tr>
 *     <td></td>
 *     <td>{@link #getAmpKfKeys(int) getAmpKfKeys(module)}</td>
 *     <td></td>
 *   </tr>
 *   <tr>
 *     <td></td>
 *     <td>{@link #getAmpKfLevels(int) getAmpKfLevels(module)}</td>
 *     <td></td>
 *   </tr>
 *   <tr></tr>
 *   <tr>
 *     <td>1-12: Velocity Level (DCA)</td>
 *     <td>{@link #getAmpVelSensCurve(int) getAmpVelSensCurve(module)}</td>
 *     <td>{@link #setAmpVelSens(int, int, int) setAmpVelSens(module,curve,sens)}</td>
 *     <td>"{@link #UID_Mm_AMP_VELLEVEL_SENSITIVITY(int) M&lt;m&gt;.AMP.VEL_LEVEL.SENSITIVITY}"</td>
 *   </tr>
 *   <tr>
 *     <td></td>
 *     <td>{@link #getAmpVelSens(int) getAmpVelSens(module)}</td>
 *     <td></td>
 *     <td>"{@link #UID_Mm_AMP_VELLEVEL_CURVE(int) M&lt;m&gt;.AMP.VEL_LEVEL.CURVE}"</td>
 *   </tr>
 *   <tr></tr>
 *   <tr>
 *     <td>1-13: Tremolo</td>
 *     <td>{@link #isTremoloMulti()}</td>
 *     <td>{@link #setTremolo(boolean, int, int, int, int) setTremolo(multi,wave,depth,rate,delay)}</td>
 *     <td>"{@link #UID_TREMOLO_MULTI TREMOLO.MULTI}"</td>
 *   </tr>
 *   <tr>
 *     <td></td>
 *     <td>{@link #getTremoloWave()}</td>
 *     <td></td>
 *     <td>"{@link #UID_TREMOLO_WAVE TREMOLO.WAVE}"</td>
 *   </tr>
 *   <tr>
 *     <td></td>
 *     <td>{@link #getTremoloDepth()}</td>
 *     <td></td>
 *     <td>"{@link #UID_TREMOLO_DEPTH TREMOLO.DEPTH}"</td>
 *   </tr>
 *   <tr>
 *     <td></td>
 *     <td>{@link #getTremoloRate()}</td>
 *     <td></td>
 *     <td>"{@link #UID_TREMOLO_RATE TREMOLO.RATE}"</td>
 *   </tr>
 *   <tr>
 *     <td></td>
 *     <td>{@link #getTremoloDelay()}</td>
 *     <td></td>
 *     <td>"{@link #UID_TREMOLO_DELAY TREMOLO.DELAY}"</td>
 *   </tr>
 *   <tr></tr>
 *   <tr>
 *     <td>1-14: Amp Sensitivity</td>
 *     <td>{@link #getAmpEffSensitivity(int) getAmpSensitivity(module)}</td>
 *     <td>{@link #setAmpEffSens(int, int) setAmpSensitivity(module,value)}</td>
 *     <td>"{@link #UID_Mm_AMPSENS(int) M&lt;m&gt;.AMP_SENS}"</td>
 *   </tr>
 *   <tr></tr>
 *   <tr>
 *     <td>1-15: Total Level</td>
 *     <td>{@link #getTotelLevel()}</td>
 *     <td>{@link #setTotalLevel(int) setTotalLevel(value)}</td>
 *     <td>"{@link #UID_TOTALLEVEL TOTAL_LEVEL}"</td>
 *   </tr>
 *   <tr></tr>
 *   <tr>
 *     <td>1-16: KF Rate (DCO/DCA)</td>
 *     <td>{@link #getRateKfKey(int) getRateKfKey(point)}</td>
 *     <td>{@link #setRateKf(String[], int[]) setRateKf(keys,rates)}</td>
 *     <td>"{@link #UID_RATE_KF_Pp_KEY(int) KF_RATE.P&lt;p&gt;.KEY}"</td>
 *   </tr>
 *   <tr>
 *     <td></td>
 *     <td>{@link #getRateKfRate(int) getRateKfRate(point)}</td>
 *     <td></td>
 *     <td>"{@link #UID_RATE_KF_Pp_RATE(int) KF_RATE.P&lt;p&gt;.RATE}"</td>
 *   </tr>
 *   <tr>
 *     <td></td>
 *     <td>{@link #getRateKfKeys()}</td>
 *     <td></td>
 *   </tr>
 *   <tr>
 *     <td></td>
 *     <td>{@link #getRateKfRates()}</td>
 *     <td></td>
 *   </tr>
 *   <tr></tr>
 *   <tr>
 *     <td>1-17: Velocity Rate (DCO/DCA)&nbsp;&nbsp;</td>
 *     <td>{@link #isAmpEnvStepVelRate(int, int) isAmpEnvStepVelRate(module,step)}</td>
 *     <td>{@link #setAmpEnvStep(int, int, int, int, boolean) setAmpEnvStep(module,step,rate,level,vrate)}</td>
 *     <td>"{@link #UID_Mm_AMP_ENV_Ss_VELRATE(int, int) M&lt;m&gt;.AMP.ENV.S&lt;s&gt;.VELOCITY_RATE}"</td>
 *   </tr>
 *   <tr>
 *     <td></td>
 *     <td>{@link #isPitchEnvStepVelRate(int) isPitchEnvStepVelRate(step)}</td>
 *     <td>{@link #setPitchEnvStep(int, int, int, boolean) setPitchEnvStep(step,rate,level,vrate)}</td>
 *     <td>"{@link #UID_PITCH_ENV_Ss_VELRATE(int) PITCH.ENV.S&lt;s&gt;.VELOCITY_RATE}"</td>
 *   </tr>
 *   <tr>
 *     <td></td>
 *     <td>{@link #getRateVelSensCurve()}</td>
 *     <td>{@link #setRateVelSens(int, int) setRateVelSens(curve,sens)}</td>
 *     <td>"{@link #UID_RATE_VELLEVEL_SENSITIVITY RATE.VEL_LEVEL.SENSITIVITY}"</td>
 *   </tr>
 *   <tr>
 *     <td></td>
 *     <td>{@link #getRateVelSens()}</td>
 *     <td></td>
 *     <td>"{@link #UID_RATE_VELLEVEL_CURVE RATE.VEL_LEVEL.CURVE}"</td>
 *   </tr>
 *   <tr></tr>
 *   <tr>
 *     <td>1-18: Voice Name</td>
 *     <td>{@link #getVoiceName()}</td>
 *     <td>{@link #setVoiceName(String) setVoiceName(name)}</td>
 *     <td>"{@link #UID_NAME_Cc(int) VOICE_NAME.C&lt;cc&gt;}"</td>
 *   </tr>
 * </table>
 * <p>Parameter UID fields</p>
 * <table style="margin-left:1em; margin-bottom:1em;">
 *   <tr>
 *     <th style="text-align:left">Field</th>
 *     <th style="text-align:left">Description</th>
 *     <th style="text-align:left">Values</th>
 *     <th style="text-align:left">See also</th>
 *   </tr>
 *   <tr>
 *     <td>{@code <cc>}</td>
 *     <td>Two-digit character number</td>
 *     <td><tt>[01,12]</tt></td>
 *     <td></td>
 *   </tr>
 *   <tr>
 *     <td>{@code <e>}</td>
 *     <td>Line ID (ext. phase)</td>
 *     <td><tt>{B,C,D}</tt></td>
 *     <td>{@link VZ1#B}, ..., {@link VZ1#D}</td>
 *   </tr>
 *   <tr>
 *     <td>{@code <l>}</td>
 *     <td>Line ID</td>
 *     <td><tt>{A,B,C,D}</tt></td>
 *     <td>{@link VZ1#A}, ..., {@link VZ1#D}</td>
 *   </tr>
 *   <tr>
 *     <td>{@code <m>}</td>
 *     <td>Module number</td>
 *     <td><tt>[1,8]</tt></td>
 *     <td>{@link VZ1#M1}, ..., {@link VZ1#M8}</td>
 *   </tr>
 *   <tr>
 *     <td>{@code <p>}</td>
 *     <td>Key-following point number</td>
 *     <td><tt>[1,6]</tt></td>
 *     <td>{@link VZ1#P1}, ..., {@link VZ1#P6}</td>
 *   </tr>
 *   <tr>
 *     <td>{@code <s>}</td>
 *     <td>Envelope step number</td>
 *     <td><tt>[1,8]</tt></td>
 *     <td>{@link VZ1#S1}, ..., {@link VZ1#S8}</td>
 *   </tr>
 * </table>
 *
 * @author Matthias Wolff
 * 
 * @see VZ-1/VZ-10M MIDI SysEx manual, section III-2
 */
public class VZ1Patch extends SyxMessage implements IPatch
{
  private static final long serialVersionUID = 1L;

  // -- Parameter UIDs --------------------------------------------------------

  // -  UID Parts - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

  private static final String int_UID_Cc(int c)
  {
    if (c<0 || c>11)
      throw SYX.IllArgExc(VZ1.E_BAD_CC,c,0,11);
    return String.format("%02d",c+1);
  }

  private static final String int_UID_Mm(int m)
  {
    if (m<VZ1.M1 || m>VZ1.M8)
      throw SYX.IllArgExc(VZ1.E_BAD_M,m,VZ1.M1,VZ1.M8);
    return String.format("M%d",m+1);
  }

  private static final String int_UID_Ll(int l)
  {
    if (l<VZ1.A || l>VZ1.D)
      throw SYX.IllArgExc(VZ1.E_BAD_L,l,VZ1.A,VZ1.D);
    return String.format("L%c",'A'+l);
  }

  private static final String int_UID_Le(int e)
  {
    if (e<VZ1.B || e>VZ1.D)
      throw SYX.IllArgExc(VZ1.E_BAD_L,e,VZ1.B,VZ1.D);
    return String.format("L%c",'A'+e);
  }

  private static final String int_UID_Pp(int p)
  {
    if (p<VZ1.P1 || p>VZ1.P6)
      throw SYX.IllArgExc(VZ1.E_BAD_P,p,VZ1.P1,VZ1.P6);
    return String.format("P%d",p+1);
  }

  private static final String int_UID_Ss(int s)
  {
    if (s<VZ1.S1 || s>VZ1.S8)
      throw SYX.IllArgExc(VZ1.E_BAD_S,s,VZ1.S1,VZ1.S8);
    return String.format("S%d",s+1);
  }

  // -  UIDs  - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

  /**
   * Parameter UID "{@code L<l>.INT_LINE}"
   * 
   * @param l
   *          The line ID &isin; <tt>{</tt>{@link VZ1#A}, ..., {@link
   *          VZ1#D}<tt>}</tt>
   */
   public static final String UID_Ll_INTLINE(int l)
  {
    return int_UID_Ll(l)+".INT_LINE";
  }

  /**
   * Parameter UID "{@code L<l>.EXT_PHASE}"
   * 
   * @param e
   *          The line ID &isin; <tt>{</tt>{@link VZ1#B}, ..., {@link
   *          VZ1#D}<tt>}</tt>
   */
  public static final String UID_Le_EXTPHASE(int e)
  {
    return int_UID_Le(e)+".EXT_PHASE";
  }

  /**
   * Parameter UID "{@code M<m>}"
   * 
   * @param m
   *          The module number &isin; <tt>{</tt>{@link VZ1#M1}, ..., {@link
   *          VZ1#M8}<tt>}</tt>
   */
  public static final String UID_Mm(int m)
  {
    return int_UID_Mm(m);
  }

  /**
   * Parameter UID "{@code M<m>.WAVE_FORM}"
   * 
   * @param m
   *          The module number &isin; <tt>{</tt>{@link VZ1#M1}, ..., {@link
   *          VZ1#M8}<tt>}</tt>
   */
  public static final String UID_Mm_WAVEFORM(int m)
  {
    return int_UID_Mm(m)+".WAVE_FORM";
  }

  /**
   * Parameter UID "{@code M<m>.DETUNE.RANGE}"
   * 
   * @param m
   *          The module number &isin; <tt>{</tt>{@link VZ1#M1}, ..., {@link
   *          VZ1#M8}<tt>}</tt>
   */
  public static final String UID_Mm_DETUNE_RANGE(int m)
  {
    return int_UID_Mm(m)+".DETUNE.RANGE";
  }

  /**
   * Parameter UID "{@code M<m>.DETUNE.PITCH_FIX}"
   * 
   * @param m
   *          The module number &isin; <tt>{</tt>{@link VZ1#M1}, ..., {@link
   *          VZ1#M8}<tt>}</tt>
   */
  public static final String UID_Mm_DETUNE_PITCHFIX(int m)
  {
    return int_UID_Mm(m)+".DETUNE.PITCH_FIX";
  }

  /**
   * Parameter UID "{@code M<m>.DETUNE.POL}"
   * 
   * @param m
   *          The module number &isin; <tt>{</tt>{@link VZ1#M1}, ..., {@link
   *          VZ1#M8}<tt>}</tt>
   */
  public static final String UID_Mm_DETUNE_POL(int m)
  {
    return int_UID_Mm(m)+".DETUNE.POL";
  }

  /**
   * Parameter UID "{@code M<m>.DETUNE.OCT_NOTE}"
   * 
   * @param m
   *          The module number &isin; <tt>{</tt>{@link VZ1#M1}, ..., {@link
   *          VZ1#M8}<tt>}</tt>
   */
  public static final String UID_Mm_DETUNE_OCTNOTE(int m)
  {
    return int_UID_Mm(m)+".DETUNE.OCT_NOTE";
  }

  /**
   * Parameter UID "{@code M<m>.DETUNE.FINE}"
   * 
   * @param m
   *          The module number &isin; <tt>{</tt>{@link VZ1#M1}, ..., {@link
   *          VZ1#M8}<tt>}</tt>
   */
  public static final String UID_Mm_DETUNE_FINE(int m)
  {
    return int_UID_Mm(m)+".DETUNE.FINE";
  }

  /**
   * Parameter UID "{@code M<m>.AMP.ENV.S<s>.RATE}"
   * 
   * @param m
   *          The module number &isin; <tt>{</tt>{@link VZ1#M1}, ..., {@link
   *          VZ1#M8}<tt>}</tt>
   * @param s
   *          The envelope step &isin; <tt>{</tt>{@link VZ1#S1}, ..., {@link
   *          VZ1#S8}<tt>}</tt>
   */
  public static final String UID_Mm_AMP_ENV_Ss_RATE(int m, int s)
  {
    return int_UID_Mm(m)+".AMP.ENV."+int_UID_Ss(s)+".RATE";
  }

  /**
   * Parameter UID "{@code M<m>.AMP.ENV.S<s>.LEV}"
   * 
   * @param m
   *          The module number &isin; <tt>{</tt>{@link VZ1#M1}, ..., {@link
   *          VZ1#M8}<tt>}</tt>
   * @param s
   *          The envelope step &isin; <tt>{</tt>{@link VZ1#S1}, ..., {@link
   *          VZ1#S8}<tt>}</tt>
   */
  public static final String UID_Mm_AMP_ENV_Ss_LEV(int m, int s)
  {
    return int_UID_Mm(m)+".AMP.ENV."+int_UID_Ss(s)+".LEV";
  }

  /**
   * Parameter UID "{@code M<m>.AMP.S<s>ENV.SS}"
   * 
   * @param m
   *          The module number &isin; <tt>{</tt>{@link VZ1#M1}, ..., {@link
   *          VZ1#M8}<tt>}</tt>
   * @param s
   *          The envelope step &isin; <tt>{</tt>{@link VZ1#S1}, ..., {@link
   *          VZ1#S8}<tt>}</tt>
   */
  public static final String UID_Mm_AMP_ENV_Ss_SS(int m, int s)
  {
    return int_UID_Mm(m)+".AMP.ENV."+int_UID_Ss(s)+".SS";
  }

  /**
   * Parameter UID "{@code M<m>.AMP.ENV.ED}"
   * 
   * @param m
   *          The module number &isin; <tt>{</tt>{@link VZ1#M1}, ..., {@link
   *          VZ1#M8}<tt>}</tt>
   */
  public static final String UID_Mm_AMP_ENV_ED(int m)
  {
    return int_UID_Mm(m)+".AMP.ENV.ED";
  }

  /**
   * Parameter UID "{@code M<m>.AMP.ENV.DEPTH}"
   * 
   * @param m
   *          The module number &isin; <tt>{</tt>{@link VZ1#M1}, ..., {@link
   *          VZ1#M8}<tt>}</tt>
   */
  public static final String UID_Mm_AMP_ENV_DEPTH(int m)
  {
    return int_UID_Mm(m)+".AMP.ENV.DEPTH";
  }

  /**
   * Parameter UID "{@code M<m>.AMP.ENV.S<s>.VELOCITY_RATE}"
   * 
   * @param m
   *          The module number &isin; <tt>{</tt>{@link VZ1#M1}, ..., {@link
   *          VZ1#M8}<tt>}</tt>
   * @param s
   *          The envelope step &isin; <tt>{</tt>{@link VZ1#S1}, ..., {@link
   *          VZ1#S8}<tt>}</tt>
   */
  public static final String UID_Mm_AMP_ENV_Ss_VELRATE(int m, int s)
  {
    return int_UID_Mm(m)+".AMP.ENV."+int_UID_Ss(s)+".VELOCITY_RATE";
  }

  /**
   * Parameter UID "{@code M<m>.AMP.KF.P<p>.KEY}"
   * 
   * @param m
   *          The module number &isin; <tt>{</tt>{@link VZ1#M1}, ..., {@link
   *          VZ1#M8}<tt>}</tt>
   * @param p
   *          The key-following support point &isin; <tt>{</tt>{@link VZ1#P1},
   *          ..., {@link VZ1#P6}<tt>}</tt>
   */
  public static final String UID_Mm_AMP_KF_Pp_KEY(int m, int p)
  {
    return int_UID_Mm(m)+".AMP.KF."+int_UID_Pp(p)+".KEY";
  }

  /**
   * Parameter UID "{@code M<m>.AMP.KF.P<p>.LEV}"
   * 
   * @param m
   *          The module number &isin; <tt>{</tt>{@link VZ1#M1}, ..., {@link
   *          VZ1#M8}<tt>}</tt>
   * @param p
   *          The key-following support point &isin; <tt>{</tt>{@link VZ1#P1},
   *          ..., {@link VZ1#P6}<tt>}</tt>
   */
  public static final String UID_Mm_AMP_KF_Pp_LEV(int m, int p)
  {
    return int_UID_Mm(m)+".AMP.KF."+int_UID_Pp(p)+".LEV";
  }

  /**
   * Parameter UID "{@code M<m>.AMP_SENS}"
   * 
   * @param m
   *          The module number &isin; <tt>{</tt>{@link VZ1#M1}, ..., {@link
   *          VZ1#M8}<tt>}</tt>
   */
  public static final String UID_Mm_AMPSENS(int m)
  {
    return int_UID_Mm(m)+".AMP_SENS";
  }

  /**
   * Parameter UID "{@code M<m>.AMP.VEL_LEVEL.SENSITIVITY}"
   * 
   * @param m
   *          The module number &isin; <tt>{</tt>{@link VZ1#M1}, ..., {@link
   *          VZ1#M8}<tt>}</tt>
   */
  public static final String UID_Mm_AMP_VELLEVEL_SENSITIVITY(int m)
  {
    return int_UID_Mm(m)+".AMP.VEL_LEVEL.SENSITIVITY";
  }

  /**
   * Parameter UID "{@code M<m>.AMP.VEL_LEVEL.CURVE}"
   * 
   * @param m
   *          The module number &isin; <tt>{</tt>{@link VZ1#M1}, ..., {@link
   *          VZ1#M8}<tt>}</tt>
   */
  public static final String UID_Mm_AMP_VELLEVEL_CURVE(int m)
  {
    return int_UID_Mm(m)+".AMP.VEL_LEVEL.CURVE";
  }

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
   * Parameter UID "{@code OCTAVE.POL}"
   */
  public static transient final String UID_OCTAVE_POL
    = "OCTAVE.POL";

  /**
   * Parameter UID "{@code OCTAVE.NUM}"
   */
  public static transient final String UID_OCTAVE_NUM
    = "OCTAVE.NUM";

  /**
   * Parameter UID "{@code PITCH.ENV.S<s>.RATE}"
   * 
   * @param s
   *          The envelope step &isin; <tt>{</tt>{@link VZ1#S1}, ..., {@link
   *          VZ1#S8}<tt>}</tt>
   */
  public static final String UID_PITCH_ENV_Ss_RATE(int s)
  {
    return "PITCH.ENV."+int_UID_Ss(s)+".RATE";
  }

  /**
   * Parameter UID "{@code PITCH.ENV.S<s>.LEV}"
   * 
   * @param s
   *          The envelope step &isin; <tt>{</tt>{@link VZ1#S1}, ..., {@link
   *          VZ1#S8}<tt>}</tt>
   */
  public static final String UID_PITCH_ENV_Ss_LEV(int s)
  {
    return "PITCH.ENV."+int_UID_Ss(s)+".LEV";
  }

  /**
   * Parameter UID "{@code PITCH.ENV.S<s>.SS}"
   * 
   * @param s
   *          The envelope step &isin; <tt>{</tt>{@link VZ1#S1}, ..., {@link
   *          VZ1#S8}<tt>}</tt>
   */
  public static final String UID_PITCH_ENV_Ss_SS(int s)
  {
    return "PITCH.ENV."+int_UID_Ss(s)+".SS";
  }

  /**
   * Parameter UID "{@code PITCH.ENV.ED}"
   */
  public static transient final String UID_PITCH_ENV_ED
    = "PITCH_ENV.ED";

  /**
   * Parameter UID "{@code PITCH_ENV.DEPTH}"
   * 
   */
  public static transient final String UID_PITCH_ENV_DEPTH
    = "PITCH_ENV.DEPTH";

  /**
   * Parameter UID "{@code PITCH_ENV.RANGE}"
   */
  public static transient final String UID_PITCH_ENV_RANGE
    = "PITCH_ENV.RANGE";

  /**
   * Parameter UID "{@code PITCH.ENV.S<s>.VELOCITYRATE}"
   * 
   * @param s
   *          The envelope step &isin; <tt>{</tt>{@link VZ1#S1}, ..., {@link
   *          VZ1#S8}<tt>}</tt>
   */
  public static final String UID_PITCH_ENV_Ss_VELRATE(int s)
  {
    return "PITCH.ENV."+int_UID_Ss(s)+".VELOCITY_RATE";
  }

  /**
   * Parameter UID "{@code PITCH.KF.P<p>.KEY}"
   * 
   * @param p
   *          The key-following support point &isin; <tt>{</tt>{@link VZ1#P1},
   *          ..., {@link VZ1#P6}<tt>}</tt>
   */
  public static final String UID_PITCH_KF_Pp_KEY(int p)
  {
    return "PITCH.KF."+int_UID_Pp(p)+".KEY";
  }

  /**
   * Parameter UID "{@code PITCH.KF.P<p>.LEV}"
   * 
   * @param p
   *          The key-following support point &isin; <tt>{</tt>{@link VZ1#P1},
   *          ..., {@link VZ1#P6}<tt>}</tt>
   */
  public static final String UID_PITCH_KF_Pp_LEV(int p)
  {
    return "PITCH.KF."+int_UID_Pp(p)+".LEV";
  }

  /**
   * Parameter UID "{@code PITCH.VEL_LEVEL.SENSITIVITY}"
   */
  public static transient final String UID_PITCH_VELLEVEL_SENSITIVITY
    = "PITCH.VEL_LEVEL.SENSITIVITY";

  /**
   * Parameter UID "{@code PITCH.VEL_LEVEL.CURVE}"
   */
  public static transient final String UID_PITCH_VELLEVEL_CURVE
    = "PITCH.VEL_LEVEL.CURVE";

  /**
   * Parameter UID "{@code RATE.KF.P<p>.KEY}"
   * 
   * @param p
   *          The key-following support point &isin; <tt>{</tt>{@link VZ1#P1},
   *          ..., {@link VZ1#P6}<tt>}</tt>
   */
  public static final String UID_RATE_KF_Pp_KEY(int p)
  {
    return "RATE.KF."+int_UID_Pp(p)+".KEY";
  }

  /**
   * Parameter UID "{@code RATE.KF.P<p>.RATE}"
   * 
   * @param p
   *          The key-following support point &isin; <tt>{</tt>{@link VZ1#P1},
   *          ..., {@link VZ1#P6}<tt>}</tt>
   */
  public static final String UID_RATE_KF_Pp_RATE(int p)
  {
    return "RATE.KF."+int_UID_Pp(p)+".RATE";
  }

  /**
   * Parameter UID "{@code RATE.VEL_LEVELCURVE}"
   */
  public static transient final String UID_RATE_VELLEVEL_CURVE
    = "RATE.VEL_LEVEL.CURVE";

  /**
   * Parameter UID "{@code RATE.VEL_LEVEL.SENSITIVITY}"
   */
  public static transient final String UID_RATE_VELLEVEL_SENSITIVITY
    = "RATE.VEL_LEVEL.SENSITIVITY";

  /**
   * Parameter UID "{@code TREMOLO.WAVE}"
   */
  public static transient final String UID_TREMOLO_WAVE
    = "TREMOLO.WAVE";

  /**
   * Parameter UID "{@code TREMOLO.DEPTH}"
   */
  public static transient final String UID_TREMOLO_DEPTH
    = "TREMOLO.DEPTH";

  /**
   * Parameter UID "{@code VIBRATO.RATE}"
   */
  public static transient final String UID_TREMOLO_RATE
    = "TREMOLO.RATE";

  /**
   * Parameter UID "{@code VIBRATO.DELAY}"
   */
  public static transient final String UID_TREMOLO_DELAY
    = "TREMOLO.DELAY";

  /**
   * Parameter UID "{@code TREMOLO.MULTI}"
   */
  public static transient final String UID_TREMOLO_MULTI
    = "TREMOLO.MULTI";

  /**
   * Parameter UID "{@code TOTAL_LEVEL}"
   */
  public static transient final String UID_TOTALLEVEL
    = "TOTAL_LEVEL";


  /**
   * Parameter UID prefix "{@code UNDOCUMENTED}"
   */
  public static transient final String UIDpfx_UNDOCUMENTED
    = "UNDOCUMENTED";

  /**
   * Parameter UID "{@code UNDOCUMENTED.I<i>}"
   * 
   * @param i
   *          The zero-based parameter number
   */
  public static final String UID_UNDOCUMENTED(int i)
  {
    return UIDpfx_UNDOCUMENTED+".I"+(i+1);
  }

  /**
   * Parameter UID "{@code VIBRATO.WAVE}"
   */
  public static transient final String UID_VIBRATO_WAVE
    = "VIBRATO.WAVE";

  /**
   * Parameter UID "{@code VIBRATO.DEPTH}"
   */
  public static transient final String UID_VIBRATO_DEPTH
    = "VIBRATO.DEPTH";

  /**
   * Parameter UID "{@code VIBRATO.RATE}"
   */
  public static transient final String UID_VIBRATO_RATE
    = "VIBRATO.RATE";

  /**
   * Parameter UID "{@code VIBRATO.DELAY}"
   */
  public static transient final String UID_VIBRATO_DELAY
    = "VIBRATO.DELAY";

  /**
   * Parameter UID "{@code VIBRATO.MULTI}"
   */
  public static transient final String UID_VIBRATO_MULTI
    = "VIBRATO.MULTI";

  // -- Constructors and Initializers -----------------------------------------

  /**
   * Creates and zero-initializes a VZ-1/VZ-10M Tone Data model.
   * 
   * @see VZ-1/VZ-10M MIDI SysEx manual, section III-2.
   */
  public VZ1Patch()
  {
    super();
    try
    {
      SyxDataStruct prt;
      SyxParamInfo  pi;

      // -  Part 0: Algorithm and Waveforms - - - - - - - - - - - - - - - - - -
      // Create part
      prt = new SyxDataStruct
      (
        "00000000 00000abc "+ // External phase lines B, C, D
        "0000ddee 0000efff "+ // Line A: Algorithm and waveforms 
        "0000gghh 0000hiii "+ // Line B: Algorithm and waveforms
        "0000jjkk 0000klll "+ // Line C: Algorithm and waveforms
        "0000mmnn 0000nooo ", // Line D: Algorithm and waveforms
        "Algorithm and Waveforms"
      );

      // Info for parameter 'a'
      pi = new SyxParamInfo(prt,'a',UID_Le_EXTPHASE(VZ1.B),"M4 external phase");
      pi.setValueMap(VZ1.VV_OFFON,VZ1.VN_OFFON);
      pi.setDefaultModelValue("OFF");
      prt.addParamInfo(pi);

      // Info for parameter 'b'
      pi = new SyxParamInfo(prt,'b',UID_Le_EXTPHASE(VZ1.C),"M6 external phase");
      pi.setValueMap(VZ1.VV_OFFON,VZ1.VN_OFFON);
      pi.setDefaultModelValue("OFF");
      prt.addParamInfo(pi);

      // Info for parameter 'c'
      pi = new SyxParamInfo(prt,'c',UID_Le_EXTPHASE(VZ1.D),"M8 external phase");
      pi.setValueMap(VZ1.VV_OFFON,VZ1.VN_OFFON);
      pi.setDefaultModelValue("OFF");
      prt.addParamInfo(pi);

      // Info for parameter 'd'
      pi = new SyxParamInfo(prt,'d',UID_Ll_INTLINE(VZ1.A),"Line A algorithm");
      pi.setValueMap(VZ1.VV_LNALG,VZ1.VN_LNALG);
      pi.setDefaultMidiValue(VZ1.MIX);
      prt.addParamInfo(pi);

      // Info for parameter 'g'
      pi = new SyxParamInfo(prt,'g',UID_Ll_INTLINE(VZ1.B),"Line B algorithm");
      pi.setValueMap(VZ1.VV_LNALG,VZ1.VN_LNALG);
      pi.setDefaultMidiValue(VZ1.MIX);
      prt.addParamInfo(pi);

      // Info for parameter 'j'
      pi = new SyxParamInfo(prt,'j',UID_Ll_INTLINE(VZ1.C),"Line C algorithm");
      pi.setValueMap(VZ1.VV_LNALG,VZ1.VN_LNALG);
      pi.setDefaultMidiValue(VZ1.MIX);
      prt.addParamInfo(pi);

      // Info for parameter 'm'
      pi = new SyxParamInfo(prt,'m',UID_Ll_INTLINE(VZ1.D),"Line D algorithm");
      pi.setValueMap(VZ1.VV_LNALG,VZ1.VN_LNALG);
      pi.setDefaultMidiValue(VZ1.MIX);
      prt.addParamInfo(pi);

      // Info for parameter 'e'
      pi = new SyxParamInfo(prt,'e',UID_Mm_WAVEFORM(VZ1.M1),"M1 waveform");
      pi.setValueMap(VZ1.VV_WFORM,VZ1.VN_WFORM);
      pi.setDefaultMidiValue(VZ1.SINE);
      prt.addParamInfo(pi);

      // Info for parameter 'f'
      pi = new SyxParamInfo(prt,'f',UID_Mm_WAVEFORM(VZ1.M2),"M2 waveform");
      pi.setValueMap(VZ1.VV_WFORM,VZ1.VN_WFORM);
      pi.setDefaultMidiValue(VZ1.SINE);
      prt.addParamInfo(pi);

      // Info for parameter 'h'
      pi = new SyxParamInfo(prt,'h',UID_Mm_WAVEFORM(VZ1.M3),"M3 waveform");
      pi.setValueMap(VZ1.VV_WFORM,VZ1.VN_WFORM);
      pi.setDefaultMidiValue(VZ1.SINE);
      prt.addParamInfo(pi);

      // Info for parameter 'i'
      pi = new SyxParamInfo(prt,'i',UID_Mm_WAVEFORM(VZ1.M4),"M4 waveform");
      pi.setValueMap(VZ1.VV_WFORM,VZ1.VN_WFORM);
      pi.setDefaultMidiValue(VZ1.SINE);
      prt.addParamInfo(pi);

      // Info for parameter 'k'
      pi = new SyxParamInfo(prt,'k',UID_Mm_WAVEFORM(VZ1.M5),"M5 waveform");
      pi.setValueMap(VZ1.VV_WFORM,VZ1.VN_WFORM);
      pi.setDefaultMidiValue(VZ1.SINE);
      prt.addParamInfo(pi);

      // Info for parameter 'l'
      pi = new SyxParamInfo(prt,'l',UID_Mm_WAVEFORM(VZ1.M6),"M6 waveform");
      pi.setValueMap(VZ1.VV_WFORM,VZ1.VN_WFORM);
      pi.setDefaultMidiValue(VZ1.SINE);
      prt.addParamInfo(pi);

      // Info for parameter 'n'
      pi = new SyxParamInfo(prt,'n',UID_Mm_WAVEFORM(VZ1.M7),"M7 waveform");
      pi.setValueMap(VZ1.VV_WFORM,VZ1.VN_WFORM);
      pi.setDefaultMidiValue(VZ1.SINE);
      prt.addParamInfo(pi);

      // Info for parameter 'o'
      pi = new SyxParamInfo(prt,'o',UID_Mm_WAVEFORM(VZ1.M8),"M8 waveform");
      pi.setValueMap(VZ1.VV_WFORM,VZ1.VN_WFORM);
      pi.setDefaultMidiValue(VZ1.SINE);
      prt.addParamInfo(pi);

      // Add part
      prt.doAssertAllParamInfos();
      addParts(prt);

      // -  Parts 1...8: Detune - - - - - - - - - - - - - - - - - - - - - - - -
      for (int m=VZ1.M1; m<=VZ1.M8; m++)
      {
        // Create part
        String mn = int_UID_Mm(m);
        prt = new SyxDataStruct
        (
          "0000ffff 0000ffxr 0000psss 0000ssss",
          mn+" detune"
        );

        // Info for parameter 'f'
        pi = new SyxParamInfo(prt,'f',UID_Mm_DETUNE_FINE(m),mn+" fine tuning");
        pi.setValueRange(0,63,0);
        pi.setDefaultMidiValue(0);
        prt.addParamInfo(pi);

        // Info for parameter 'x'
        pi = new SyxParamInfo(prt,'x',UID_Mm_DETUNE_PITCHFIX(m),mn+" pitch fix");
        pi.setValueMap(VZ1.VV_OFFON,VZ1.VN_OFFON);
        pi.setDefaultModelValue("OFF");
        prt.addParamInfo(pi);

        // Info for parameter 'r'
        pi = new SyxParamInfo(prt,'r',UID_Mm_DETUNE_RANGE(m),mn+" range");
        pi.setValueMap(VZ1.VV_DETRNG,VZ1.VN_DETRNG);
        pi.setDefaultModelValue("x1");
        prt.addParamInfo(pi);

        // Info for parameter 'p'
        pi = new SyxParamInfo(prt,'p',UID_Mm_DETUNE_POL(m),mn+" polarity");
        pi.setValueMap(VZ1.VV_POL,VZ1.VN_POL);
        pi.setDefaultModelValue("+");
        prt.addParamInfo(pi);

        // Info for parameter 's'
        SyxDataStruct thisPrt = prt;
        pi = new SyxParamInfo(prt,'s',UID_Mm_DETUNE_OCTNOTE(m),mn+" semitones")
        {
          private static final long serialVersionUID = 1L;

          @Override
          public void validateMidiValue(int midiValue, boolean onInit)
          throws InvalidMidiDataException
          {
            super.validateMidiValue(midiValue,onInit);
            if (onInit)
              // Do not check dependencies
              return;

            // Check dependencies
            if (thisPrt.getMidiValue('x')==0/*OFF*/)
              if (midiValue>0x47)
                throw SYX.InvMdataExc
                (
                  "MIDI value 0x%02X is out of range [0x00...0x47] "
                  + "(pitch fix off)",
                  midiValue
                );
          }
        };
        pi.setValueRange(0x00,0x7F,0x00);
        pi.setDefaultMidiValue(0x00);
        prt.addParamInfo(pi);

        // Add part
        prt.doAssertAllParamInfos();
        addParts(prt);
      }

      // -  Parts 9...24: Amplitude & Pitch Envelope  - - - - - - - - - - - - -
      for (int s=VZ1.S1; s<=VZ1.S8; s++)
      {
        // Create velocity rates & rates part
        prt = new SyxDataStruct
        (
          "0000abbb 0000bbbb 0000cddd 0000dddd "+
          "0000efff 0000ffff 0000ghhh 0000hhhh "+
          "0000ijjj 0000jjjj 0000klll 0000llll "+
          "0000mnnn 0000nnnn 0000oppp 0000pppp "+
          "0000qrrr 0000rrrr ",
          "Amp./pitch env. velocity rates & rates "+int_UID_Ss(s)
        );

        // Add velocity rates & rates parameter infos
        for (int m=VZ1.M1; m<=VZ1.M8+1/*Extra m is pitch envelope*/; m++)
        {
          String UID_VRATE;
          String UID_RATE;
          String descr_VRATE;
          String descr_RATE;
          if (m<=VZ1.M8)
          { // Amplitude envelope
            UID_VRATE   = UID_Mm_AMP_ENV_Ss_VELRATE(m,s);
            UID_RATE    = UID_Mm_AMP_ENV_Ss_RATE        (m,s);
            descr_VRATE = int_UID_Mm(m)+" velocity rate " +s;
            descr_RATE  = int_UID_Mm(m)+" amp. env. rate "+s;
          }
          else
          { // Pitch envelope
            UID_VRATE   = UID_PITCH_ENV_Ss_VELRATE (s);
            UID_RATE    = UID_PITCH_ENV_Ss_RATE         (s);
            descr_VRATE = "Pitch velocity rate "+s;
            descr_RATE  = "Pitch env. rate "    +s;
          }

          // Info for velocity rate parameters 'a', 'c', ..., 'q'
          pi = new SyxParamInfo(prt,(char)('a'+2*m),UID_VRATE,descr_VRATE);
          pi.setValueMap(VZ1.VV_VELRATE,VZ1.VN_VELRATE);
          pi.setDefaultModelValue("*");
          prt.addParamInfo(pi);

          // Info for rate parameters 'b', 'd', ..., 'r'
          pi = new SyxParamInfo(prt,(char)('b'+2*m),UID_RATE,descr_RATE);
          pi.setValueMap(VZ1.note2_midiValues(),VZ1.note2_modelValues());
          pi.setDefaultModelValue((s==VZ1.S1 && m!=VZ1.M8+1)?99:50);
          prt.addParamInfo(pi);
        }

        // Add velocity rates & rates part
        prt.doAssertAllParamInfos();
        addParts(prt);

        // Create sustain flags & levels part
        prt = new SyxDataStruct
        (
          "0000abbb 0000bbbb 0000cddd 0000dddd "+
          "0000efff 0000ffff 0000ghhh 0000hhhh "+
          "0000ijjj 0000jjjj 0000klll 0000llll "+
          "0000mnnn 0000nnnn 0000oppp 0000pppp "+
          "0000qrrr 0000rrrr ",
          "Amp./pitch sustain flags & env. levels "+int_UID_Ss(s)
        );

        // Add sustain flags & levels parameter infos
        for (int m=VZ1.M1; m<=VZ1.M8+1/*Extra m is pitch envelope*/; m++)
        {
          String UID_SS;
          String UID_LEV;
          String descr_SS;
          String descr_LEV;
          if (m<=VZ1.M8)
          { // Amplitude envelope
            UID_SS    = UID_Mm_AMP_ENV_Ss_SS (m,s);
            UID_LEV   = UID_Mm_AMP_ENV_Ss_LEV(m,s);
            descr_SS  = int_UID_Mm(m)+" amp. env. sustain "+s;
            descr_LEV = int_UID_Mm(m)+" amp. env. level "  +s;
          }
          else
          { // Pitch envelope
            UID_SS    = UID_PITCH_ENV_Ss_SS (s);
            UID_LEV   = UID_PITCH_ENV_Ss_LEV(s);
            descr_SS  = "Pitch env. sustain "+s;
            descr_LEV = "Pitch env. level "  +s;
          }

          // Info for sustain parameters 'a', 'c', ..., 'q' 
          pi = new SyxParamInfo(prt,(char)('a'+2*m),UID_SS,descr_SS);
          pi.setValueMap(VZ1.VV_SUSTAIN,VZ1.VN_SUSTAIN);
          pi.setDefaultModelValue(s==VZ1.S1 ? "SUS" : "NO");
          prt.addParamInfo(pi);

          // Info for level parameters 'b', 'd', ..., 'r' 
          pi = new SyxParamInfo(prt,(char)('b'+2*m),UID_LEV,descr_LEV);
          if (m<=VZ1.M8)
            // M1...M8 Amplitude envelopes
            pi.setValueMap(VZ1.note3_midiValues(),VZ1.note3_modelValues());
          else
            // Pitch envelope
            pi.setValueRange
            (
              VZ1.note4_minMidiValue,
              VZ1.note4_maxMidiValue,
              VZ1.note4_modelValueOffset
            );
          pi.setDefaultModelValue((s==VZ1.S1 && m!=VZ1.M8+1)?99:0);
          prt.addParamInfo(pi);
        }

        // Add sustain flags & levels part
        prt.doAssertAllParamInfos();
        addParts(prt);
      }

      // -  Part 25: Amplitude Envelope End Step & Sensitivity  - - - - - - - -
      // Create part
      prt = new SyxDataStruct
      (
        "00000aaa 00000bbb 00000ccc 00000ddd "+
        "00000eee 00000fff 00000ggg 00000hhh "+
        "00000iii 00000jjj 00000kkk 00000lll "+
        "00000mmm 00000nnn 00000ooo 00000ppp ",
        "Amp. env. end step & sens."
      );

     // Add parameter infos
      for (int m=VZ1.M1; m<=VZ1.M8; m++)
      {
        String UID_ED   = UID_Mm_AMP_ENV_ED(m);
        String UID_AS   = UID_Mm_AMPSENS   (m);
        String descr_ED = int_UID_Mm(m)+" amp. env. end step";
        String descr_AS = int_UID_Mm(m)+" amp. env. sensitivity";

        // Info for end step parameters 'a', 'c', ..., 'o'
        pi = new SyxParamInfo(prt,(char)('a'+2*m),UID_ED,descr_ED);
        pi.setValueRange(VZ1.S1,VZ1.S8,0);
        pi.setDefaultMidiValue(VZ1.S8);
        prt.addParamInfo(pi);

        // Info for amplitude sensitivity parameters 'b', 'd', ..., 'p' 
        pi = new SyxParamInfo(prt,(char)('b'+2*m),UID_AS,descr_AS);
        pi.setValueRange(0x00,0x07,0);
        pi.setDefaultMidiValue(0x00);
        prt.addParamInfo(pi);
      }

      // Add part
      prt.doAssertAllParamInfos();
      addParts(prt);

      // -  Part 26: Pitch Envelope End Step  - - - - - - - - - - - - - - - - -
      // Create part
      prt = new SyxDataStruct("00000sss 00000000","Pitch env. end step");

      // Add parameter info
      pi = new SyxParamInfo(prt,'s',UID_PITCH_ENV_ED,"Pitch env. end step");
      pi.setValueRange(VZ1.S1,VZ1.S8,0);
      pi.setDefaultMidiValue(VZ1.S8);
      prt.addParamInfo(pi);

      // Add part
      prt.doAssertAllParamInfos();
      addParts(prt);

      // -  Part 27: Total Level  - - - - - - - - - - - - - - - - - - - - - - -
      // Create part
      prt = new SyxDataStruct("00000lll 0000llll","Totel level");

      // Add parameter info
      pi = new SyxParamInfo(prt,'l',UID_TOTALLEVEL,"Total level");
      pi.setValueMap(VZ1.note5_midiValues(),VZ1.note5_modelValues());
      pi.setDefaultModelValue(99);
      prt.addParamInfo(pi);

      // Add part
      prt.doAssertAllParamInfos();
      addParts(prt);

      // -  Part 28: Amplitude Envelope Depth & Module On - - - - - - - - - - -
      // Create part
      prt = new SyxDataStruct
      (
        "0000abbb 0000bbbb 0000cddd 0000dddd "+
        "0000efff 0000ffff 0000ghhh 0000hhhh "+
        "0000ijjj 0000jjjj 0000klll 0000llll "+
        "0000mnnn 0000nnnn 0000oppp 0000pppp ",
        "Amp. env. depth / module on"
      );
      // Add parameter infos
      for (int m=VZ1.M1; m<=VZ1.M8; m++)
      {
        String UID_M     = UID_Mm              (m);
        String UID_AED   = UID_Mm_AMP_ENV_DEPTH(m);
        String descr_M   = int_UID_Mm(m)+" on/off";
        String descr_AED = int_UID_Mm(m)+" amp. env. depth";

        // Info for module on/off parameters 'a', 'c', ..., 'o'
        pi = new SyxParamInfo(prt,(char)('a'+2*m),UID_M,descr_M);
        pi.setValueMap(VZ1.VV_ONOFF,VZ1.VN_ONOFF);
        pi.setDefaultModelValue("ON");
        prt.addParamInfo(pi);

        // Info for amplitude envelope depth parameters 'b', 'd', ..., 'p' 
        pi = new SyxParamInfo(prt,(char)('b'+2*m),UID_AED,descr_AED);
        pi.setValueMap(VZ1.note5_midiValues(),VZ1.note5_modelValues());
        pi.setDefaultModelValue(99);
        prt.addParamInfo(pi);
      }

      // Add part
      prt.doAssertAllParamInfos();
      addParts(prt);

      // -  Part 29: Pitch Envelope Depth & Range - - - - - - - - - - - - - - -
      // Create part
      prt = new SyxDataStruct("0000r0dd 0000dddd","Pitch env. depth & range");

      // Info for parameter 'r'
      pi = new SyxParamInfo(prt,'r',UID_PITCH_ENV_RANGE,"Pitch env. range");
      pi.setValueMap(VZ1.VV_PENVRNG,VZ1.VN_PENVRNG);
      pi.setDefaultModelValue("NARROW");
      prt.addParamInfo(pi);

      // Info for parameter 'd'
      pi = new SyxParamInfo(prt,'d',UID_PITCH_ENV_DEPTH,"Pitch env. depth");
      pi.setValueMap(VZ1.note6_midiValues(),VZ1.note6_modelValues());
      pi.setDefaultModelValue(63);
      prt.addParamInfo(pi);

      // Add part
      prt.doAssertAllParamInfos();
      addParts(prt);

      // -  Parts 30...39: Key-Following  - - - - - - - - - - - - - - - - - - -
      for (int m=VZ1.M1; m<=VZ1.M8+2/*Extra ms are pitch and rate KF*/; m++)
      {
        // Create part
        String prtName;
        if (m>=VZ1.M1 && m<=VZ1.M8)
          // Amplitude KF
          prtName = int_UID_Mm(m)+" amp. key following";
        else if (m==VZ1.M8+1)
          // Pitch KF
          prtName = "Pitch key following";
        else
          // Rate KF
          prtName = "Rate key following";
        prt = new SyxDataStruct
        (
          "00000aaa 0000aaaa 00000bbb 0000bbbb "+
          "00000ccc 0000cccc 00000ddd 0000dddd "+
          "00000eee 0000eeee 00000fff 0000ffff "+
          "00000ggg 0000gggg 00000hhh 0000hhhh "+
          "00000iii 0000iiii 00000jjj 0000jjjj "+
          "00000kkk 0000kkkk 00000lll 0000llll ",
          prtName
        );

        // Add parameter infos
        // - Default key values
        String [] defModelValue_KEY = new String[]
        {
          "C2", "F4", "C7", "A7", "E8", "C9"
        };

        // - Pattern of key UID (for validator)
        String UID_KEY_f;
        if (m>=VZ1.M1 && m<=VZ1.M8)
          // Amplitude KF
          UID_KEY_f = UID_Mm_AMP_KF_Pp_KEY(m,VZ1.P1);
        else if (m==VZ1.M8+1)
          // Pitch KF
          UID_KEY_f = UID_PITCH_KF_Pp_KEY(VZ1.P1);
        else
          // Rate KF
          UID_KEY_f = UID_RATE_KF_Pp_KEY(VZ1.P1);
        UID_KEY_f = UID_KEY_f.replace(".P1.",".P$.");

        // - Loop over key-following support points
        for (int p=VZ1.P1; p<=VZ1.P6; p++)
        {
          String UID_KEY;
          String UID_VAL;
          String descr_KEY;
          String descr_VAL;
          int[]  midiValues_VAL;
          int[]  modelValues_VAL;
          int    defModelValue_VAL;
          if (m>=VZ1.M1 && m<=VZ1.M8)
          { // Amplitude KF
            UID_KEY           = UID_Mm_AMP_KF_Pp_KEY(m,p);
            UID_VAL           = UID_Mm_AMP_KF_Pp_LEV(m,p);
            descr_KEY         = int_UID_Mm(m)+" key follow key "  +int_UID_Pp(p);
            descr_VAL         = int_UID_Mm(m)+" key follow level "+int_UID_Pp(p);
            midiValues_VAL    = VZ1.note5_midiValues();
            modelValues_VAL   = VZ1.note5_modelValues();
            defModelValue_VAL = 99;
          }
          else if (m==VZ1.M8+1)
          { // Pitch KF
            UID_KEY           = UID_PITCH_KF_Pp_KEY(p);
            UID_VAL           = UID_PITCH_KF_Pp_LEV(p);
            descr_KEY         = "Pitch key follow key "  +int_UID_Pp(p);
            descr_VAL         = "Pitch key follow level "+int_UID_Pp(p);
            midiValues_VAL    = VZ1.note6_midiValues();
            modelValues_VAL   = VZ1.note6_modelValues();
            defModelValue_VAL = 63;
          }
          else
          { // Rate KF
            UID_KEY           = UID_RATE_KF_Pp_KEY (p);
            UID_VAL           = UID_RATE_KF_Pp_RATE(p);
            descr_KEY         = "Rate key follow key " +int_UID_Pp(p);
            descr_VAL         = "Rate key follow rate "+int_UID_Pp(p);
            midiValues_VAL    = VZ1.note2_midiValues();
            modelValues_VAL   = VZ1.note2_modelValues();
            defModelValue_VAL = 0;
          }

          // Info for key parameters 'a', 'c', ..., 'k'
          int           thisP   = p;
          SyxDataStruct thisPrt = prt;
          String  thisUID_KEY_f = UID_KEY_f;
          pi = new SyxParamInfo(prt,(char)('a'+2*p),UID_KEY,descr_KEY)
          {
            private static final long serialVersionUID = 1L;

            @Override
            public void validateMidiValue(int midiValue, boolean onInit)
            throws InvalidMidiDataException
            {
              super.validateMidiValue(midiValue,onInit);
              if (onInit)
                // Do not check dependencies
                return;

              // Check dependencies
              // - Get previous and next key-following support point
              int prevKey = 0;
              int nextKey = Integer.MAX_VALUE;
              if (thisP>VZ1.P1)
                prevKey = thisPrt.getMidiValue((char)(this.getPname()-2));
              if (thisP<VZ1.P6)
                nextKey = thisPrt.getMidiValue((char)(this.getPname()+2));

              // - Check ascending order of support points
              if (prevKey>=midiValue)
                throw SYX.InvMdataExc
                (
                  "%s: Key %s<-0x%02X must be greater than key %s=0x%02X",
                  thisUID_KEY_f,int_UID_Pp(thisP),midiValue,int_UID_Pp(thisP-1),
                  prevKey
                );
              if (nextKey<=midiValue)
                throw SYX.InvMdataExc
                (
                  "%s: Key %s<-0x%02X must be less than key %s=0x%02X",
                  thisUID_KEY_f,int_UID_Pp(thisP),midiValue,int_UID_Pp(thisP+1),
                  nextKey
                );
            }
          };
          pi.setValueMap(VZ1.note7_midiValues(p),VZ1.note7_valueNames(p));
          pi.setDefaultModelValue(defModelValue_KEY[p]);
          prt.addParamInfo(pi);

          // Info for value parameters 'b', 'd', ..., 'l'
          pi = new SyxParamInfo(prt,(char)('b'+2*p),UID_VAL,descr_VAL);
          pi.setValueMap(midiValues_VAL,modelValues_VAL);
          pi.setDefaultModelValue(defModelValue_VAL);
          prt.addParamInfo(pi);
        }

        // Add part
        prt.doAssertAllParamInfos();
        addParts(prt);
      }

      // -  Part 40: Velocity Sensitivity - - - - - - - - - - - - - - - - - - -
      // Create part
      prt = new SyxDataStruct
      (
        "0000aaab 0000bbbb 0000cccd 0000dddd "+
        "0000eeef 0000ffff 0000gggh 0000hhhh "+
        "0000iiij 0000jjjj 0000kkkl 0000llll "+
        "0000mmmn 0000nnnn 0000ooop 0000pppp "+
        "0000qqqr 0000rrrr 0000ssst 0000tttt ",
        "Velocity sens."
      );

      // Add parameter infos
      for (int m=VZ1.M1; m<=VZ1.M8+2/*Extra ms are pitch and rate*/; m++)
      {
        String UID_CURVE;
        String UID_SENS;
        String descr_CURVE;
        String descr_SENS;
        if (m>=VZ1.M1 && m<=VZ1.M8)
        { // Amplitude sensitivity
          UID_CURVE   = UID_Mm_AMP_VELLEVEL_CURVE      (m);
          UID_SENS    = UID_Mm_AMP_VELLEVEL_SENSITIVITY(m);
          descr_CURVE = "M"+int_UID_Mm(m)+" vel. sens. curve";
          descr_SENS  = "M"+int_UID_Mm(m)+" vel. sensitivity";
        }
        else if (m==VZ1.M8+1)
        { // Pitch sensitivity
          UID_CURVE   = UID_PITCH_VELLEVEL_CURVE;
          UID_SENS    = UID_PITCH_VELLEVEL_SENSITIVITY;
          descr_CURVE = "Pitch vel. sens. curve";
          descr_SENS  = "Pitch vel. sensitivity";
        }
        else
        { // Rate sensitivity
          UID_CURVE   = UID_RATE_VELLEVEL_CURVE;
          UID_SENS    = UID_RATE_VELLEVEL_SENSITIVITY;
          descr_CURVE = "Rate vel. sens. curve";
          descr_SENS  = "Rate vel. sensitivity";
        }

        // Info for velocity sensitivity curve parameters 'a', 'c', ..., 's'
        pi = new SyxParamInfo(prt,(char)('a'+2*m),UID_CURVE,descr_CURVE);
        pi.setValueMap(VZ1.VV_VSCURVE,VZ1.VN_VSCURVE);
        pi.setDefaultModelValue(1);
        prt.addParamInfo(pi);

        // Info for velocity sensitivity parameters 'b', 'd', ..., 't'
        pi = new SyxParamInfo(prt,(char)('b'+2*m),UID_SENS,descr_SENS);
        pi.setValueRange(0x00,0x1F,0);
        pi.setDefaultModelValue(0);
        prt.addParamInfo(pi);
      }

      // Add part
      prt.doAssertAllParamInfos();
      addParts(prt);

      // -  Parts 41, 42: Octave, Vibrato & Tremolo - - - - - - - - - - - - - -
      for (int i=41; i<=42; i++)
      {
        String type;
        String UID_MULTI;
        String UID_WAVE;
        String UID_DEPTH;
        String UID_RATE;
        String UID_DELAY;

        // Create part
        if (i==41)
        { // Octave & Vibrato
          prt = new SyxDataStruct
          (
            "0000poo0 0000a0bb 00000ccc 0000cccc "+
            "00000ddd 0000dddd 00000eee 0000eeee ",
            "Octave & Vibrato"
          );
          type      = "Vibrato";
          UID_MULTI = UID_VIBRATO_MULTI;
          UID_WAVE  = UID_VIBRATO_WAVE;
          UID_DEPTH = UID_VIBRATO_DEPTH;
          UID_RATE  = UID_VIBRATO_RATE;
          UID_DELAY = UID_VIBRATO_DELAY;
        }
        else
        { // Tremolo
          prt = new SyxDataStruct
          (
            "00000000 0000a0bb 00000ccc 0000cccc "+
            "00000ddd 0000dddd 00000eee 0000eeee ",
            "Tremolo"
          );
          type      = "Tremolo";
          UID_MULTI = UID_TREMOLO_MULTI;
          UID_WAVE  = UID_TREMOLO_WAVE;
          UID_DEPTH = UID_TREMOLO_DEPTH;
          UID_RATE  = UID_TREMOLO_RATE;
          UID_DELAY = UID_TREMOLO_DELAY;
        }

        // Add parameter infos
        if (i==41)
        {
          // Info for octave polarity parameter 'p'
          pi = new SyxParamInfo(prt,'p',UID_OCTAVE_POL,"Octave polarity");
          pi.setValueMap(VZ1.VV_POL,VZ1.VN_POL);
          pi.setDefaultModelValue("+");
          prt.addParamInfo(pi);

          // Info for octave number parameter 'o'
          pi = new SyxParamInfo(prt,'o',UID_OCTAVE_NUM,"Octave number");
          pi.setValueRange(0x00,0x02,0);
          pi.setDefaultModelValue(0);
          prt.addParamInfo(pi);
        }

        // Info for multi parameter 'a'
        pi = new SyxParamInfo(prt,'a',UID_MULTI,type+" multi");
        pi.setValueMap(VZ1.VV_OFFON,VZ1.VN_OFFON);
        pi.setDefaultModelValue("OFF");
        prt.addParamInfo(pi);

        // Info for wave parameter 'b'
        pi = new SyxParamInfo(prt,'b',UID_WAVE,type+" wave");
        pi.setValueMap(VZ1.VV_VTWAVE,VZ1.VN_VTWAVE);
        pi.setDefaultModelValue("TRIANGLE");
        prt.addParamInfo(pi);

        // Info for depth parameter 'c'
        pi = new SyxParamInfo(prt,'c',UID_DEPTH,type+" depth");
        pi.setValueRange(0x00,0x63,0);
        pi.setDefaultModelValue(0);
        prt.addParamInfo(pi);

        // Info for rate parameter 'd'
        pi = new SyxParamInfo(prt,'d',UID_RATE,type+" rate");
        pi.setValueRange(0x00,0x63,0);
        pi.setDefaultModelValue(75);
        prt.addParamInfo(pi);

        // Info for delay parameter 'e'
        pi = new SyxParamInfo(prt,'e',UID_DELAY,type+" delay");
        pi.setValueRange(0x00,0x63,0);
        pi.setDefaultModelValue(0);
        prt.addParamInfo(pi);

        // Add part
        prt.doAssertAllParamInfos();
        addParts(prt);
      }

      // -  Part 43: Voice Name - - - - - - - - - - - - - - - - - - - - - - - -
      // Create part
      prt = new SyxDataStruct
      (
        "0000aaaa 0000aaaa 0000bbbb 0000bbbb "+ // Characters  1,  2
        "0000cccc 0000cccc 0000dddd 0000dddd "+ // Characters  3,  4
        "0000eeee 0000eeee 0000ffff 0000ffff "+ // Characters  5,  6
        "0000gggg 0000gggg 0000hhhh 0000hhhh "+ // Characters  7,  8
        "0000iiii 0000iiii 0000jjjj 0000jjjj "+ // Characters  9, 10
        "0000kkkk 0000kkkk 0000llll 0000llll ", // Characters 11, 12
        "Voice name"
      );

      // Add parameter infos
      //               |0         1 |
      //               |012345678901|
      String defName = "INIT VOICE  ";
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

      // -  Part 44: (undocumented) - - - - - - - - - - - - - - - - - - - - - -
      // Create part
      prt = new SyxDataStruct("0aH 0aH 0bH 0bH","(undocumented)");

      // Add parameter infos
      for (int i=0; i<=1; i++)
      {
        String descr = "(undocumented)";
        pi = new SyxParamInfo(prt,(char)('a'+i),UID_UNDOCUMENTED(i),descr);
        pi.setValueRange(0x20,0x7F,0);
        pi.setDefaultModelValue(0x20);
        prt.addParamInfo(pi);
      }

      // Add part
      prt.doAssertAllParamInfos();
      addParts(prt);

      // -- Part 45: Checksum - - - - - - - - - - - - - - - - - - - - - - - - -
      addParts(new SyxChecksum(SyxChecksum.CASIO_VZ,0,-1));

      // -- Initialize patch data ---------------------------------------------
      reset();
    }
    catch (Throwable e)
    {// Should not happen
      throw SYX.InternalError(e);
    }
  }

  /**
   * Creates a VZ-1/VZ-10M Tone Data model and initializes it from a byte array.
   * 
   * <p><b>Note:</b> The constructor does neither validate nor initialize the 
   * checksum. Invoke {@link #validateChecksum()} to validate the checksum in 
   * {@code data} or {@link #updateChecksum()} to update the checksum in this
   * model!</p>
   * 
   * @param data
   *          A byte array containing VZ Tone Data, including the checksum. The
   *          array <em>must not</em> begin with a system exclusive status byte
   *          (0xF0 or 0xF7). A tailing end-of-exclusive byte (EOX, 0xF7) will
   *          be ignored. If initializing with data from a {@link SyxMessage}
   *          message, use {@code data=}{@link SyxMessage#getData()}!
   * @throws InvalidMidiDataException
   *          if {@code data} contains ill-formatted or incompatible data 
   * @see VZ-1/VZ-10M MIDI SysEx manual, section III-2. 
   */
  public VZ1Patch(byte[] data)
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
   * @see VZ-1 User Manual, p. 100
   */
  @Override
  public void reset()
  {
    super.reset();
  }

  // -- API: Getters and Setters ----------------------------------------------

  /**
   * Gets the on/off state of a module.
   *
   * @param module
   *          The module &isin; {{@link VZ1#M1}, ..., {@link VZ1#M8}}
   * @return {@code true} if the module is on, {@code false} if it is off
   * @throws IllegalArgumentException if the argument is out of range
   * 
   * @see VZ-1 User Manual, p. 23
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 11
   */
  public boolean isModuleOn(int module)
  throws IllegalArgumentException
  {
    return getModelValueAsString(UID_Mm(module))=="ON";
  }

  /**
   * Sets the on/off state of a module.
   *
   * @param module
   *          The module &isin; {{@link VZ1#M1}, ..., {@link VZ1#M8}}
   * @param on 
   *          {@code true} to switch the module on, {@code false} to switch it
   *          off
   * @throws IllegalArgumentException if argument {@code module} is out of range
   * 
   * @see VZ-1 User Manual, p. 23
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 11
   */
  public void setModuleOn(int module, boolean on)
  throws IllegalArgumentException
  {
    try
    {
      setModelValue(UID_Mm(module),on?"ON":"OFF");
    }
    catch (InvalidMidiDataException e)
    { // One of the arguments is illegal, hence...
      throw new IllegalArgumentException(e);
    }
    finally
    {
      updateChecksum();
    }
  }

  // -  VZ-Menu 1-00: LINE  - - - - - - - - - - - - - - - - - - - - - - - - - -

  /**
   * Gets a line algorithm.
   *
   * @param line
   *          The line &isin; {{@link VZ1#A}, ..., {@link VZ1#D}}
   * @return The algorithm &isin; {{@link VZ1#MIX}, {@link VZ1#PHASE}, {@link
   *         VZ1#RING}}
   * @throws IllegalArgumentException if the argument is out of range
   * 
   * @see VZ-1 User Manual, Menu 1-00, p. 33
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 9
   */
  public int getLineAlgorithm(int line)
  throws IllegalArgumentException
  {
    return getMidiValue(UID_Ll_INTLINE(line));
  }

  /**
   * Sets a line algorithm.
   *
   * @param line
   *          The line &isin; {{@link VZ1#A}, ..., {@link VZ1#D}}
   * @param alg 
   *          The algorithm &isin; {{@link VZ1#MIX}, {@link VZ1#PHASE}, {@link
   *          VZ1#RING}}
   * @throws IllegalArgumentException if either argument is out of range
   * 
   * @see VZ-1 User Manual, Menu 1-00, p. 33
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 9
   */
  public void setLineAlgoritm(int line, int alg)
  throws IllegalArgumentException
  {
    try
    {
      setMidiValue(UID_Ll_INTLINE(line),alg);
    }
    catch (InvalidMidiDataException e)
    { // One of the arguments is illegal, hence...
      throw new IllegalArgumentException(e);
    }
    finally
    {
      updateChecksum();
    }
  }

  /**
   * Gets the external phase flag of a line algorithm.
   *
   * @param line
   *          The line &isin; {{@link VZ1#B}, ..., {@link VZ1#D}}
   * @return The flag
   * @throws IllegalArgumentException if the argument is out of range
   * 
   * @see VZ-1 User Manual, Menu 1-00, p. 33
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 9
   */
  public boolean isExtPhase(int line)
  throws IllegalArgumentException
  {
    return getModelValueAsString(UID_Le_EXTPHASE(line))=="ON";
  }

  /**
   * Sets the external phase flag of a line algorithm.
   *
   * @param line
   *          The line &isin; {{@link VZ1#B}, ..., {@link VZ1#D}}
   * @param on
   *          The flag
   * @throws IllegalArgumentException if argument {@code line} is out of range
   * @see VZ-1 User Manual, Menu 1-00, p. 33
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 9
   */
  public void setExtPhase(int line, boolean on)
  throws IllegalArgumentException
  {
    try
    {
      setModelValue(UID_Le_EXTPHASE(line),on?"ON":"OFF");
    }
    catch (InvalidMidiDataException e)
    { // One of the arguments is illegal, hence...
      throw new IllegalArgumentException(e);
    }
    finally
    {
      updateChecksum();
    }
  }

  // -  VZ-Menu 1-01: WAVEFORM  - - - - - - - - - - - - - - - - - - - - - - - -

  /**
   * Gets a module waveform.
   *
   * @param module
   *          The module &isin; {{@link VZ1#M1}, ..., {@link VZ1#M8}}
   * @return The waveform &isin; {{@link VZ1#SINE}, {@link VZ1#SAW1}, ...,
   *         {@link VZ1#SAW5}, {@link VZ1#NOISE1}, {@link VZ1#NOISE2}}
   * @throws IllegalArgumentException if the argument is out of range
   * 
   * @see VZ-1 User Manual, Menu 1-01, p. 34
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 9
   */
  public int getWaveform(int module)
  throws IllegalArgumentException
  {
    return getMidiValue(UID_Mm_WAVEFORM(module));
  }

  /**
   * Sets a module waveform.
   *
   * @param module
   *          The module &isin; {{@link VZ1#M1}, ..., {@link VZ1#M8}}
   * @param wform 
   *          The waveform &isin; {{@link VZ1#SINE}, {@link VZ1#SAW1}, ...,
   *          {@link VZ1#SAW5}, {@link VZ1#NOISE1}, {@link VZ1#NOISE2}}
   * @throws IllegalArgumentException if either argument is out of range
   * 
   * @see VZ-1 User Manual, Menu 1-01, p. 34
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 9
   */
  public void setWaveform(int module, int wform)
  throws IllegalArgumentException
  {
    try
    {
      setMidiValue(UID_Mm_WAVEFORM(module),wform);
    }
    catch (InvalidMidiDataException e)
    { // One of the arguments is illegal, hence...
      throw new IllegalArgumentException(e);
    }
    finally
    {
      updateChecksum();
    }
  }

  // -  VZ-Menu 1-02: DETUNE  - - - - - - - - - - - - - - - - - - - - - - - - -

  /**
   * Gets the detune polarity of a module.
   *
   * @param module
   *          The module &isin; {{@link VZ1#M1}, ..., {@link VZ1#M8}}
   * @return The polarity &isin; {-1,1}
   * @throws IllegalArgumentException if the argument is out of range
   * @see VZ-1 User Manual, Menu 1-02, p. 35
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 9
   */
  public int getDetunePolarity(int module)
  throws IllegalArgumentException
  {
    if (getDetuneMode(module)==VZ1.REL)
      return getModelValueAsString(UID_Mm_DETUNE_POL(module))=="+"?+1:-1;
    else
      return +1;
  }

  /**
   * Gets the detuning number of octaves of a module.
   *
   * @param module
   *          The module &isin; {{@link VZ1#M1}, ..., {@link VZ1#M8}}
   * @return The number of octaves, &isin; [0,5] if {@linkplain
   *         #getDetuneMode(int) pitch fix} is off, and &isin; [0,10] if pitch
   *         fix is on
   * @throws IllegalArgumentException if the argument is out of range
   * @see VZ-1 User Manual, Menu 1-02, p. 35
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 9, 15
   */
  public int getDetuneOctave(int module)
  throws IllegalArgumentException
  {
    return getModelValue(UID_Mm_DETUNE_OCTNOTE(module)) / 12;
  }

  /**
   * Gets the detuning number of notes (i.e., semitones) within the octave of a 
   * module.
   *
   * @param module
   *          The module &isin; {{@link VZ1#M1}, ..., {@link VZ1#M8}}
   * @return The number of notes, &isin; [0,11] in {@linkplain
   *         #getDetuneOctave(int) octaves} 0...9, and &isin; [0,7] in octave 10 
   * @throws IllegalArgumentException if the argument is out of range
   * @see VZ-1 User Manual, Menu 1-02, p. 35
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 9, 15
   */
  public int getDetuneNote(int module)
  throws IllegalArgumentException
  {
    return getModelValue(UID_Mm_DETUNE_OCTNOTE(module)) % 12;
  }

  /**
   * Gets the fine detuning of a module.
   *
   * @param module
   *          The module &isin; {{@link VZ1#M1}, ..., {@link VZ1#M8}}
   * @return The fine detuning &isin; [0,63]
   * @throws IllegalArgumentException if the argument is out of range
   * 
   * @see VZ-1 User Manual, Menu 1-02, p. 35
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 9
   */
  public int getDetuneFine(int module)
  throws IllegalArgumentException
  {
    return getModelValue(UID_Mm_DETUNE_FINE(module));
  }

  /**
   * Gets the detune mode of a module.
   * 
   * @param module
   *          The module &isin; {{@link VZ1#M1}, ..., {@link VZ1#M8}}
   * @return The detune mode &isin; {{@link VZ1#REL}, {@link VZ1#FIXHI}, {@link 
   *          VZ1#FIXLO}}
   * @throws IllegalArgumentException if the argument is out of range
   * 
   * @see VZ-1 User Manual, Menu 1-02, p. 35
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 9
   */
  public int getDetuneMode(int module)
  throws IllegalArgumentException
  {
    String r = getModelValueAsString(UID_Mm_DETUNE_RANGE   (module));
    String x = getModelValueAsString(UID_Mm_DETUNE_PITCHFIX(module));

    if (x=="OFF")
      return VZ1.REL;
    else
      if (r=="x1/16")
        return VZ1.FIXLO;
      else
        return VZ1.FIXHI;
  }

  /**
   * Gets the detuning of a module {@linkplain SYX#encodeDetune(int, int, int)
   * encoded} in a float.
   * 
   * @param module
   *          The module &isin; {{@link VZ1#M1}, ..., {@link VZ1#M8}}
   * @return The detuning in semitones, decimal places denote fine detuning in
   *         cents
   * @throws IllegalArgumentException if the argument is out of range
   * 
   * @see VZ-1 User Manual, Menu 1-02, p. 35
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 9
   */
  public float getDetune(int module)
  {
    int pol  = getDetunePolarity(module);
    int oct  = getDetuneOctave(module);
    int note = getDetuneNote(module);
    int fine = getDetuneFine(module); 
    
    return SYX.encodeDetune(pol,oct*12+note,fine);
  }

  /**
   * Sets the relative detuning of a module, i.e., PITCH FIX = OFF.
   *
   * @param module
   *          The module &isin; {{@link VZ1#M1}, ..., {@link VZ1#M8}}
   * @param pol
   *         The polarity &isin; {-1,1}
   * @param oct
   *         The number of octaves &isin; [0,5]
   * @param note
   *         The number of notes (i.e., semitones) within the octave &isin;
   *         [0,11] 
   * @param fine
   *         The fine tuning &isin; [0,63]
   * @throws IllegalArgumentException if any argument is out of range
   * 
   * @see #setDetuneRel(int, float)
   * @see #setDetuneFix(int, int, int, int, int, boolean)
   * @see VZ-1 User Manual, Menu 1-02, p. 35
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 9
   */
  public void setDetuneRel(int module, int pol, int oct, int note, int fine)
  throws IllegalArgumentException
  {
    try
    {
      setModelValue(UID_Mm_DETUNE_PITCHFIX(module),"OFF"        );
      setModelValue(UID_Mm_DETUNE_RANGE   (module),"x1"         );
      setModelValue(UID_Mm_DETUNE_POL     (module),pol<0?"-":"+");
      setModelValue(UID_Mm_DETUNE_OCTNOTE (module),oct*12+note  );
      setModelValue(UID_Mm_DETUNE_FINE    (module),fine         );
    }
    catch (InvalidMidiDataException e)
    { // One of the arguments is illegal, hence...
      throw new IllegalArgumentException(e);
    }
    finally
    {
      updateChecksum();
    }
  }

  /**
   * Sets the relative detuning of a module&mdash;i.e.,
   * <tt>PITCH&hairsp;FIX=OFF</tt>&mdash; from a {@linkplain
   * SYX#encodeDetune(String, int, int) float value}.
   * 
   * @param module
   *          The module &isin; {{@link VZ1#M1}, ..., {@link VZ1#M8}}
   * @param detune
   *          The detuning in semitones, decimal places denote fine detuning
   *          in cents
   * @throws IllegalArgumentException if any argument is out of range
   * 
   * @see #getDetune(int)
   * @see #setDetuneFix(int, float, boolean)
   * @see VZ-1 User Manual, Menu 1-02, p. 35
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 9
   */
  public void setDetuneRel(int module, float detune)
  {
    int pol  = SYX.decodeDetunePol(detune);
    int oct  = SYX.decodeDetuneSemitones(detune) / 12;
    int note = SYX.decodeDetuneSemitones(detune) % 12;
    int fine = SYX.decodeDetuneCents(detune);

    setDetuneRel(module,pol,oct,note,fine);
  }

  /**
   * Sets fix detuning of a module, i.e., <tt>PITCH&hairsp;FIX=ON</tt>.
   *
   * @param module
   *          The module &isin; {{@link VZ1#M1}, ..., {@link VZ1#M8}}
   * @param oct
   *         The number of octaves &isin; [0,10]
   * @param note
   *         The number of notes (i.e., semitones) within the octave, &isin;
   *         [0,11] if {@code oct} in octave 0...9, and &isin; [0,7] in octave
   *         10
   * @param fine
   *         The fine tuning &isin; [0,63]
   * @param low
   *         If {@code true}, select low detune range {@code (x1/16)}, otherwise
   *         select high detune range {@code (x1)}
   * @throws IllegalArgumentException if either argument is out of range
   * 
   * @see #setDetuneRel(int, int, int, int, int)
   * @see VZ-1 User Manual, Menu 1-02, p. 35
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 9
   */
  public void setDetuneFix(int module, int oct, int note, int fine, boolean low)
  throws IllegalArgumentException
  {
    try
    {
      setModelValue(UID_Mm_DETUNE_PITCHFIX(module),"ON"            );
      setModelValue(UID_Mm_DETUNE_RANGE   (module),low?"x1/16":"x1");
      setModelValue(UID_Mm_DETUNE_POL     (module),+1              );
      setModelValue(UID_Mm_DETUNE_OCTNOTE (module),oct*12+note     );
      setModelValue(UID_Mm_DETUNE_FINE    (module),fine            );
    }
    catch (InvalidMidiDataException e)
    { // One of the arguments is illegal, hence...
      throw new IllegalArgumentException(e);
    }
    finally
    {
      updateChecksum();
    }
  }

  /**
   * Sets the fix detuning of a module&mdash;i.e., <tt>PITCH&hairsp;FIX=ON</tt
   * >&mdash; from a {@linkplain SYX#encodeDetune(String, int, int) float
   * value}.
   * 
   * @param module
   *          The module &isin; {{@link VZ1#M1}, ..., {@link VZ1#M8}}
   * @param detune
   *          The detuning in semitones, decimal places denote fine detuning;
   *          must not be negative
   * @param low
   *         If {@code true}, select low detune range {@code (x1/16)}, otherwise
   *         select high detune range {@code (x1)}
   *          in cents
   * @throws IllegalArgumentException if any argument is out of range
   * 
   * @see #getDetune(int)
   * @see #setDetuneRel(int, float)
   * @see VZ-1 User Manual, Menu 1-02, p. 35
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 9
   */
  public void setDetuneFix(int module, float detune, boolean low)
  {
    if (SYX.decodeDetunePol(detune)<0)
      throw SYX.IllArgExc(SYX.E_ARG_INVAL,detune,"detune");

    int oct  = SYX.decodeDetuneSemitones(detune) / 12;
    int note = SYX.decodeDetuneSemitones(detune) % 12;
    int fine = SYX.decodeDetuneCents(detune);

    setDetuneFix(module,oct,note,fine,low);
  }

  // -  VZ-Menu 1-03: ENVELOPE (DCO)  - - - - - - - - - - - - - - - - - - - - -

  /**
   * Gets the pitch envelope rate for an envelope step.
   * 
   * @param step
   *          The envelope step &isin; {{@link VZ1#S1}, ..., {@link VZ1#S8}}
   * @return The amplitude envelope rate &isin; [0,99]
   * @throws IllegalArgumentException
   *          if the argument is out of range
   *  
   * @see VZ-1 User Manual, Menu 1-03 (p. 36)
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 10
   */
  public int getPitchEnvStepRate(int step)
  throws IllegalArgumentException
  {
    return getModelValue(UID_PITCH_ENV_Ss_RATE(step));
  }

  /**
   * Gets the pitch envelope level for an envelope step.
   * 
   * @param step
   *          The envelope step &isin; {{@link VZ1#S1}, ..., {@link VZ1#S8}}
   * @return The pitch envelope level &isin; [-63,63]
   * @throws IllegalArgumentException
   *          if the argument is out of range
   *  
   * @see VZ-1 User Manual, Menu 1-03 (p. 36)
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 10
   */
  public int getPitchEnvStepLevel(int step)
  throws IllegalArgumentException
  {
    return getModelValue(UID_PITCH_ENV_Ss_LEV(step));
  }

  /**
   * Gets the pitch envelope sustain step.
   * 
   * @return The step &isin; {{@link VZ1#S1}, ..., {@link VZ1#S8}}, or -1 if no
   *         sustain set is set
   * 
   * @see VZ-1 User Manual, Menu 1-03 (p. 36)
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 10
   */
  public int getPitchEnvSustainStep()
  {
    for (int s=VZ1.S1; s<=VZ1.S8; s++)
      if (getModelValueAsString(UID_PITCH_ENV_Ss_SS(s))=="SUS")
        return s;
    return -1;
  }

  /**
   * Gets the pitch envelope end step.
   * 
   * @return The step &isin; {{@link VZ1#S1}, ..., {@link VZ1#S8}}
   * 
   * @see VZ-1 User Manual, Menu 1-03 (p. 36)
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 11
   */
  public int getPitchEnvEndStep()
  {
    return getMidiValue(UID_PITCH_ENV_ED);
  }

  /**
   * Sets the pitch envelope for an envelope step.
   * 
   * @param step
   *          The envelope step &isin; {{@link VZ1#S1}, ..., {@link VZ1#S8}}
   * @param rate
   *          The step rate &isin; [0,99]
   * @param level
   *          The step level &isin; [-63,63]
   * @throws IllegalArgumentException
   *           if any argument is out of range 
   * 
   * @see VZ-1 User Manual, Menu 1-03 (p. 36), Menu 1-17 (p. 50)
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 10
   */
  public void setPitchEnvStep(int step, int rate, int level)
  throws IllegalArgumentException
  {
    try
    {
      setModelValue(UID_PITCH_ENV_Ss_RATE(step),rate );
      setModelValue(UID_PITCH_ENV_Ss_LEV (step),level);
    }
    catch (InvalidMidiDataException e)
    { // One of the arguments is illegal, hence...
      throw new IllegalArgumentException(e);
    }
    finally
    {
      updateChecksum();
    }
  }

  /**
   * Sets the pitch envelope sustain step.
   * 
   * @param step
   *          The envelope step &isin; {{@link VZ1#S1}, ..., {@link VZ1#S8}}, or
   *          -1 to disable sustain
   * @throws IllegalArgumentException
   *          if the argument is out of range
   *  
   * @see VZ-1 User Manual, Menu 1-03 (p. 36)
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 10
   */
  public void setPitchEnvSustainStep(int step)
  throws IllegalArgumentException
  {
    if (!SYX.valueIn(step,VZ1.S1,VZ1.S8) && step!=-1)
      throw SYX.IllArgExc(SYX.E_ARG_INVAL,step,"step");
    try
    {
      for (int s=VZ1.S1; s<=VZ1.S8; s++)
        setModelValue(UID_PITCH_ENV_Ss_SS(s),step==s?"SUS":"NO");
    }
    catch (InvalidMidiDataException e)
    { // One of the arguments is illegal, hence...
      throw new IllegalArgumentException(e);
    }
    finally
    {
      updateChecksum();
    }
  }

  /**
   * Sets the pitch envelope end step.
   * 
   * @param step
   *          The envelope step in {{@link VZ1#S1}, ..., {@link VZ1#S8}}
   * @throws IllegalArgumentException
   *          if the argument is out of range 
   * @see VZ-1 User Manual, Menu 1-03 (p. 36)
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 11
   */
  public void setPitchEnvEndStep(int step)
  throws IllegalArgumentException
  {
    try
    {
      setMidiValue(UID_PITCH_ENV_ED,step);
    }
    catch (InvalidMidiDataException e)
    { // One of the arguments is illegal, hence...
      throw new IllegalArgumentException(e);
    }
    finally
    {
      updateChecksum();
    }
  }

  // -  VZ-Menu 1-04: ENVELOPE DEPTH (DCO)  - - - - - - - - - - - - - - - - - -

  /**
   * Gets the pitch envelope range.
   *
   * @return The pitch envelope depth &isin; {{@link VZ1#NARROW},
   *         {@link VZ1#WIDE}}
   *
   * @see VZ-1 User Manual, Menu 1-04, p. 37
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 11
   */
  public int getPitchEnvRange()
  {
    return getMidiValue(UID_PITCH_ENV_RANGE);
  }

  /**
   * Gets the pitch envelope depth.
   *
   * @return The pitch envelope depth &isin; [0,63]
   *
   * @see VZ-1 User Manual, Menu 1-04, p. 37
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 11
   */
  public int getPitchEnvDepth()
  {
    return getModelValue(UID_PITCH_ENV_DEPTH);
  }

  /**
   * Sets the pitch envelope range.
   *
   * @param range 
   *          The pitch envelope range &isin; {{@link VZ1#NARROW},
   *          {@link VZ1#WIDE}}
   * @throws IllegalArgumentException if the argument is out of range
   * 
   * @see VZ-1 User Manual, Menu 1-04, p. 37
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 11
   */
  public void setPitchEnvRange(int range)
  {
    try
    {
      setMidiValue(UID_PITCH_ENV_RANGE,range);
    }
    catch (InvalidMidiDataException e)
    { // One of the arguments is illegal, hence...
      throw new IllegalArgumentException(e);
    }
    finally
    {
      updateChecksum();
    }
  }

  /**
   * Sets the pitch envelope depth.
   *
   * @param depth 
   *          The pitch envelope depth &isin; [0,63]
   * @throws IllegalArgumentException if the argument is out of range
   * 
   * @see VZ-1 User Manual, Menu 1-04, p. 37
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 11
   */
  public void setPitchEnvDepth(int depth)
  {
    try
    {
      setModelValue(UID_PITCH_ENV_DEPTH,depth);
    }
    catch (InvalidMidiDataException e)
    { // One of the arguments is illegal, hence...
      throw new IllegalArgumentException(e);
    }
    finally
    {
      updateChecksum();
    }
  }

  // -  VZ-Menu 1-05: KF LEVEL (DCO)  - - - - - - - - - - - - - - - - - - - - -

  /**
   * Gets an pitch key-following key name (key on the keyboard) of a
   * key-following support point.
   * 
   * @param point
   *         The support point &isin; {{@link VZ1#P1}, ..., {@link VZ1#P6}}
   * @return The "display" data value &isin; {'C0', 'C#0', ..., 'C9'}
   * @throws IllegalArgumentException if the argument is out of range
   * 
   * @see VZ-1 User Manual, Menu 1-05, p. 38
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 12
   */
  public String getPitchKfKey(int point)
  throws IllegalArgumentException
  {
    return getModelValueAsString(UID_PITCH_KF_Pp_KEY(point));
  }

  /**
   * Gets an pitch key-following level of a key-following support point.
   * 
   * @param point
   *         The support point &isin; {{@link VZ1#P1}, ..., {@link VZ1#P6}}
   * @return The "display" data value &isin; [0,63]
   * @throws IllegalArgumentException if the argument is out of range
   * 
   * @see VZ-1 User Manual, Menu 1-05, p. 38
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 12
   */
  public int getPitchKfLevel(int point)
  throws IllegalArgumentException
  {
    return getModelValue(UID_PITCH_KF_Pp_LEV(point));
  }

  /**
   * Gets the pitch key-following key names (keys on the keyboard).
   * 
   * @return An array of exactly six key names &isin; {'C0', 'C#0', ..., 'C9'}
   * 
   * @see VZ-1 User Manual, Menu 1-05, p. 38
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 12
   */
  public String[] getPitchKfKeys()
  {
    String keys[] = new String[6];
    for (int point=VZ1.P1; point<=VZ1.P6; point++)
      keys[point-VZ1.P1] = getPitchKfKey(point);
    return keys;
  }

  /**
   * Gets the pitch key-following levels.
   * 
   * @return An array of exactly six levels &isin; [0,63]
   * 
   * @see VZ-1 User Manual, Menu 1-05, p. 38
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 12
   */
  public int[] getPitchKfLevels()
  {
    int levels[] = new int[6];
    for (int point=VZ1.P1; point<=VZ1.P6; point++)
      levels[point-VZ1.P1] = getPitchKfLevel(point);
    return levels;
  }

  /**
   * Sets the pitch key-following parameters.
   * 
   * @param keys
   *         An array of exactly six key names in the following ranges
   *         <ul style="margin-bottom:0">
   *           <li>{@code keys[0]} &isin; {'C0', ..., 'G8'},</li>
   *           <li>{@code keys[1]} &isin; {'C#0', ..., 'Ab8'},</li>
   *           <li>{@code keys[2]} &isin; {'D0', ..., 'A8'},</li>
   *           <li>{@code keys[3]} &isin; {'Eb0', ..., 'Bb8'},</li>
   *           <li>{@code keys[4]} &isin; {'E0', ..., 'B8'}, and</li>
   *           <li>{@code keys[5]} &isin; {'F0', ..., 'C9'}</li>
   *         </ul>
   *         <span style="margin-left:1em">{@code keys[0]}&lt; {@code
   *         keys[1]}&lt; {@code keys[2]}&lt;{@code keys[3]}&lt; {@code
   *         keys[4]}&lt; {@code keys[5]} must hold.</span>
   * @param levels
   *          An array of exactly six levels, each &isin; [0,63]
   * @throws IllegalArgumentException if
   *          <ul style="margin-bottom:0">
   *            <li>{@code keys} or {@code levels} is {@code null} or does not
   *              contain exactly six values, or</li>
   *            <li>any value in {@code keys} or {@code levels} is not
   *              permissible</li>
   * @see VZ-1 User Manual, Menu 1-05, p. 38
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 12
   */
  public void setPitchKf(String[] keys, int[] levels)
  {
    String UIDpfx = UID_PITCH_KF_Pp_KEY(VZ1.P1).replace(".P1.KEY","");
    int_setKf(UIDpfx,keys,levels);
    // NOTE: updateChecksum() invoked by int_setKf(...) 
  }

  // -  VZ-Menu 1-06: VELOCITY LEVEL (DCO)  - - - - - - - - - - - - - - - - - -

  /**
   * Gets the pitch velocity sensitivity curve.
   *
   * @return The pitch velocity sensitivity curve &isin; {{@link VZ1#C1}, ... 
   *          {@link VZ1#C8}}
   * 
   * @see VZ-1 User Manual, Menu 1-06, p. 39
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 13
   */
  public int getPitchVelSensCurve()  throws IllegalArgumentException
  {
    return getMidiValue(UID_PITCH_VELLEVEL_CURVE);
  }

  /**
   * Gets the pitch velocity sensitivity.
   *
   * @return The pitch velocity sensitivity &isin; [0,31]
   * 
   * @see VZ-1 User Manual, Menu 1-06, p. 39
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 13
   */
  public int getPitchVelSens()
  {
    return getModelValue(UID_PITCH_VELLEVEL_SENSITIVITY);
  }

  /**
   * Sets the pitch velocity sensitivity.
   * 
   * @param curve
   *          The pitch velocity sensitivity curve &isin; {{@link VZ1#C1}, ... 
   *          {@link VZ1#C8}}
   * @param sens
   *          The pitch velocity sensitivity &isin; [0,31]
   * @throws IllegalArgumentException if either argument is out of range
   * 
   * @see VZ-1 User Manual, Menu 1-06, p. 39
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 13
   */
  public void setPitchVelSens(int curve, int sens)
  throws IllegalArgumentException
  {
    try
    {
      setMidiValue (UID_PITCH_VELLEVEL_CURVE      ,curve);
      setModelValue(UID_PITCH_VELLEVEL_SENSITIVITY,sens );
    }
    catch (InvalidMidiDataException e)
    { // One of the arguments is illegal, hence...
      throw new IllegalArgumentException(e);
    }
    finally
    {
      updateChecksum();
    }
  }

  // -  VZ-Menu 1-07: VIBRATO (DCO) - - - - - - - - - - - - - - - - - - - - - -

  /**
   * Returns the vibrato multi flag.
   * 
   * @return {@code true} if the vibrato effect is triggered separately or every
   *         note played, {@code false} if the effect is synchronized over all
   *         simultaneously sounding notes. 
   * 
   * @see VZ-1 User Manual, Menu 1-07, p. 40
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 13
   */
  public boolean isVibratoMulti()
  {
    return getModelValueAsString(UID_VIBRATO_MULTI)=="ON";
  }

  /**
   * Returns the vibrato waveform &isin; {{@link VZ1#TRIANG}, {@link VZ1#SAWUP}, 
   * {@link VZ1#SAWDN}, {@link VZ1#SQUARE}}.
   * 
   * @see VZ-1 User Manual, Menu 1-07, p. 40
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 13
   */
  public int getVibratoWave()
  {
    return getMidiValue(UID_VIBRATO_WAVE);
  }

  /**
   * Returns the vibrato effect depth &isin; [0,99].
   * 
   * @see VZ-1 User Manual, Menu 1-07, p. 40
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 13
   */
  public int getVibratoDepth()
  {
    return getModelValue(UID_VIBRATO_DEPTH);
  }

  /**
   * Returns the vibrato effect rate &isin; [0,99].
   * 
   * @see VZ-1 User Manual, Menu 1-07, p. 40
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 13
   */
  public int getVibratoRate()
  {
    return getModelValue(UID_VIBRATO_RATE);
  }

  /**
   * Returns the vibrato effect delay &isin; [0,99].
   * 
   * @see VZ-1 User Manual, Menu 1-07, p. 40
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 13
   */
  public int getVibratoDelay()
  {
    return getModelValue(UID_VIBRATO_DELAY);
  }

  /**
   * Sets the vibrato effect parameters.
   * 
   * @param multi
   *          The vibrato multi flag, {@code true} if the vibrato effect is 
   *          triggered separately or every note played, {@code false} if the 
   *          effect is synchronized over all simultaneously sounding notes
   * @param wave
   *          The vibrato waveform &isin; {{@link VZ1#TRIANG}, {@link#
   *          VZ1#SAWUP}, {@link VZ1#SAWDN}, {@link VZ1#SQUARE}}
   * @param depth
   *          The vibrato effect depth &isin; [0,99]
   * @param rate
   *          The vibrato effect rate &isin; [0,99]
   * @param delay
   *          The vibrato effect delay &isin; [0,99]
   * @throws IllegalArgumentException if any argument is out of range
   * 
   * @see VZ-1 User Manual, Menu 1-07, p. 40
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 13
   */
  public void setVibrato(boolean multi, int wave, int depth, int rate, int delay)
  throws IllegalArgumentException
  {
    try
    {
      setModelValue(UID_VIBRATO_MULTI,multi?"ON":"OFF");
      setMidiValue (UID_VIBRATO_WAVE ,wave            );
      setModelValue(UID_VIBRATO_DEPTH,depth           );
      setModelValue(UID_VIBRATO_RATE ,rate            );
      setModelValue(UID_VIBRATO_DELAY,delay           );
    }
    catch (InvalidMidiDataException e)
    { // One of the arguments is illegal, hence...
      throw new IllegalArgumentException(e);
    }
    finally
    {
      updateChecksum();
    }
  }

  // -  VZ-Menu 1-08: OCTAVE  - - - - - - - - - - - - - - - - - - - - - - - - -

  /**
   * Returns the global octave shift &isin; [-2,2].
   * 
   * @see VZ-1 User Manual, Menu 1-08, p. 41
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 13
   */
  public int getOctave()
  {
    int p = getModelValueAsString(UID_OCTAVE_POL)=="+"?+1:-1;
    int o = getModelValue        (UID_OCTAVE_NUM);
    return p*o;
  }

  /**
   * Sets the global octave shift
   * 
   * @param value
   *          The octave shift &isin; [-2,2]
   * @throws IllegalArgumentException if the argument is out of range
   * 
   * @see VZ-1 User Manual, Menu 1-08, p. 41
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 13
   */
  public void setOctave(int value)
  throws IllegalArgumentException
  {
    try
    {
      setModelValue(UID_OCTAVE_POL,value<0?"-":"+");
      setModelValue(UID_OCTAVE_NUM,Math.abs(value));
    }
    catch (InvalidMidiDataException e)
    { // One of the arguments is illegal, hence...
      throw new IllegalArgumentException(e);
    }
    finally
    {
      updateChecksum();
    }
  }

  // -  VZ-Menu 1-09: ENVELOPE (DCA)  - - - - - - - - - - - - - - - - - - - - -

  /**
   * Gets the amplitude envelope rate for a module and an envelope step.
   * 
   * @param module
   *          The module &isin; {@link VZ1#M1}, ..., {@link VZ1#M8}
   * @param step
   *          The envelope step &isin; {{@link VZ1#S1}, ..., {@link VZ1#S8}}
   * @return The amplitude envelope rate &isin; [0,99]
   * @throws IllegalArgumentException
   *          if either argument is out of range 
   * 
   * @see VZ-1 User Manual, Menu 1-09, p. 42
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 10
   */
  public int getAmpEnvStepRate(int module, int step)
  throws IllegalArgumentException
  {
    return getModelValue(UID_Mm_AMP_ENV_Ss_RATE(module,step));
  }

  /**
   * Gets the amplitude envelope level for a module and an envelope step.
   * 
   * @param module
   *          The module &isin; {@link VZ1#M1}, ..., {@link VZ1#M8}
   * @param step
   *          The envelope step &isin; {{@link VZ1#S1}, ..., {@link VZ1#S8}}
   * @return The amplitude envelope level &isin; [0,99]
   * @throws IllegalArgumentException
   *          if either argument is out of range
   *  
   * @see VZ-1 User Manual, Menu 1-09, p. 42
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 10
   */
  public int getAmpEnvStepLevel(int module, int step)
  throws IllegalArgumentException
  {
    return getModelValue(UID_Mm_AMP_ENV_Ss_LEV(module,step));
  }

  /**
   * Gets the amplitude envelope sustain step for a module.
   * 
   * @param module
   *          The module &isin; {@link VZ1#M1}, ..., {@link VZ1#M8}
   * @return The step &isin; {{@link VZ1#S1}, ..., {@link VZ1#S8}}, or -1 if no
   *         sustain set is set
   * @throws IllegalArgumentException
   *          if the argument is out of range
   * 
   * @see VZ-1 User Manual, Menu 1-09, p. 42
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 10
   */
  public int getAmpEnvSustainStep(int module)
  throws IllegalArgumentException
  {
    for (int s=VZ1.S1; s<=VZ1.S8; s++)
      if (getModelValueAsString(UID_Mm_AMP_ENV_Ss_SS(module,s))=="SUS")
        return s;
    return -1;
  }

  /**
   * Gets the amplitude envelope end step for a module.
   * 
   * @param module
   *          The module &isin; {@link VZ1#M1}, ..., {@link VZ1#M8}
   * @return The end step &isin; {{@link VZ1#S1}, ..., {@link VZ1#S8}}
   * @throws IllegalArgumentException
   *          if the argument is out of range
   * 
   * @see VZ-1 User Manual, Menu 1-09, p. 42
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 11
   */
  public int getAmpEnvEndStep(int module)
  {
    return getModelValue(UID_Mm_AMP_ENV_ED(module));
  }

  /**
   * Sets the amplitude envelope for a module and an envelope step.
   * 
   * @param module
   *          The module &isin; {{@link VZ1#M1}, ..., {@link VZ1#M8}}
   * @param step
   *          The envelope step &isin; {{@link VZ1#S1}, ..., {@link VZ1#S8}}
   * @param rate
   *          The step rate &isin; [0,99]
   * @param level
   *          The step level &isin; [0,99]
   * @throws IllegalArgumentException
   *           if any argument is out of range
   *  
   * @see VZ-1 User Manual, Menu 1-09, p. 42
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 10
   */
  public void setAmpEnvStep(int module, int step, int rate, int level)
  throws IllegalArgumentException
  {
    try
    {
      setModelValue(UID_Mm_AMP_ENV_Ss_RATE(module,step),rate );
      setModelValue(UID_Mm_AMP_ENV_Ss_LEV (module,step),level);
    }
    catch (InvalidMidiDataException e)
    { // One of the arguments is illegal, hence...
      throw new IllegalArgumentException(e);
    }
    finally
    {
      updateChecksum();
    }
  }

  /**
   * Sets the amplitude envelope sustain step for a module.
   * 
   * @param module
   *          The module &isin; {@link VZ1#M1}, ..., {@link VZ1#M8}
   * @param step
   *          The envelope step &isin; {{@link VZ1#S1}, ..., {@link VZ1#S8}}, or
   *          -1 to disable sustain
   * @throws IllegalArgumentException
   *          if any argument is out of range 
   * 
   * @see VZ-1 User Manual, Menu 1-09, p. 42
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 10
   */
  public void setAmpEnvSustainStep(int module, int step)
  throws IllegalArgumentException
  {
    if (!SYX.valueIn(step,VZ1.S1,VZ1.S8) && step!=-1)
      throw SYX.IllArgExc(SYX.E_ARG_INVAL,step,"step");
    try
    {
      for (int s=VZ1.S1; s<=VZ1.S8; s++)
        setModelValue(UID_Mm_AMP_ENV_Ss_SS(module,s),step==s?"SUS":"NO");
    }
    catch (InvalidMidiDataException e)
    { // One of the arguments is illegal, hence...
      throw new IllegalArgumentException(e);
    }
    finally
    {
      updateChecksum();
    }
  }

  /**
   * Sets the amplitude envelope end step for a module.
   * 
   * @param module
   *          The module &isin; {{@link VZ1#M1}, ..., {@link VZ1#M8}}
   * @param step
   *          The envelope step &isin; {{@link VZ1#S1}, ..., {@link VZ1#S8}}
   * @throws IllegalArgumentException
   *          if either argument is out of range
   * 
   * @see VZ-1 User Manual, Menu 1-09, p. 42
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 11
   */
  public void setAmpEnvEndStep(int module, int step)
  {
    try
    {
      setMidiValue(UID_Mm_AMP_ENV_ED(module),step);
    }
    catch (InvalidMidiDataException e)
    { // One of the arguments is illegal, hence...
      throw new IllegalArgumentException(e);
    }
    finally
    {
      updateChecksum();
    }
  }

  // -  VZ-Menu 1-10: ENV DEPTH (DCA) - - - - - - - - - - - - - - - - - - - - -

  /**
   * Gets the amplitude envelope depth of a module.
   *
   * @param module
   *          The module &isin; {{@link VZ1#M1}, ..., {@link VZ1#M8}}
   * @return The amplitude envelope depth &isin; [0,99]
   * @throws IllegalArgumentException if the argument is out of range
   * 
   * @see VZ-1 User Manual, Menu 1-10, p. 43
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, pp. 11
   */
  public int getAmpEnvDepth(int module)
  throws IllegalArgumentException
  {
    return getModelValue(UID_Mm_AMP_ENV_DEPTH(module));
  }

  /**
   * Sets the amplitude envelope depth of a module.
   *
   * @param module
   *          The module &isin; {{@link VZ1#M1}, ..., {@link VZ1#M8}}
   * @param value 
   *          The amplitude envelope depth &isin; [0,99]
   * @throws IllegalArgumentException if either argument is out of range
   * 
   * @see VZ-1 User Manual, Menu 1-10, p. 43
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, pp. 11
   */
  public void setAmpEnvDepth(int module, int value)
  throws IllegalArgumentException
  {
    try
    {
      setModelValue(UID_Mm_AMP_ENV_DEPTH(module),value);
    }
    catch (InvalidMidiDataException e)
    { // One of the arguments is illegal, hence...
      throw new IllegalArgumentException(e);
    }
    finally
    {
      updateChecksum();
    }
  }

  // -  VZ-Menu 1-11: KF LEVEL (DCA)  - - - - - - - - - - - - - - - - - - - - -

  /**
   * Gets an amplitude key-following key name (key on the keyboard) of a 
   * key-following support point for a module.
   * 
   * @param module
   *          The module &isin; {{@link VZ1#M1}, ..., {@link VZ1#M8}}
   * @param point
   *         The support point &isin; {{@link VZ1#P1}, ..., {@link VZ1#P6}}
   * @return The "display" data value &isin; {'C0', 'C#0', ..., 'C9'}
   * @throws IllegalArgumentException if either argument is out of range
   * 
   * @see VZ-1 User Manual, Menu 1-11, p. 44
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 12
   */
  public String getAmpKfKey(int module, int point)
  throws IllegalArgumentException
  {
    return getModelValueAsString(UID_Mm_AMP_KF_Pp_KEY(module,point));
  }

  /**
   * Gets an amplitude key-following level of a key-following support point for
   * a module.
   * 
   * @param module
   *          The module &isin; {{@link VZ1#M1}, ..., {@link VZ1#M8}}
   * @param point
   *         The support point &isin; {{@link VZ1#P1}, ..., {@link VZ1#P6}}
   * @return The "display" data value &isin; [0,99]
   * @throws IllegalArgumentException if either argument is out of range
   * 
   * @see VZ-1 User Manual, Menu 1-11, p. 44
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 12
   */
  public int getAmpKfLevel(int module, int point)
  throws IllegalArgumentException
  {
    return getModelValue(UID_Mm_AMP_KF_Pp_LEV(module,point));
  }

  /**
   * Gets the amplitude key-following key names (keys on the keyboard) for a 
   * module.
   * 
   * @param module
   *          The module &isin; {{@link VZ1#M1}, ..., {@link VZ1#M8}}
   * @return An array of exactly six key names &isin; {'C0', 'C#0', ..., 'C9'}
   * @throws IllegalArgumentException if the argument is out of range
   * 
   * @see VZ-1 User Manual, Menu 1-11, p. 44
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 12
   */
  public String[] getAmpKfKeys(int module)
  {
    String keys[] = new String[6];
    for (int point=VZ1.P1; point<=VZ1.P6; point++)
      keys[point-VZ1.P1] = getAmpKfKey(module,point);
    return keys;
  }

  /**
   * Gets the amplitude key-following levels for a module.
   * 
   * @param module
   *          The module &isin; {{@link VZ1#M1}, ..., {@link VZ1#M8}}
   * @return An array of exactly levels &isin; [0,99]
   * @throws IllegalArgumentException if the argument is out of range
   * 
   * @see VZ-1 User Manual, Menu 1-11, p. 44
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 12
   */
  public int[] getAmpKfLevels(int module)
  {
    int levels[] = new int[6];
    for (int point=VZ1.P1; point<=VZ1.P6; point++)
      levels[point-VZ1.P1] = getAmpKfLevel(module,point);
    return levels;
  }

  /**
   * Sets the amplitude key-following parameters for a module.
   * 
   * @param module
   *          The module &isin; {{@link VZ1#M1}, ..., {@link VZ1#M8}}
   * @param keys
   *         An array of exactly six key names in the following ranges
   *         <ul style="margin-bottom:0">
   *           <li>{@code keys[0]} &isin; {'C0', ..., 'G8'},</li>
   *           <li>{@code keys[1]} &isin; {'C#0', ..., 'Ab8'},</li>
   *           <li>{@code keys[2]} &isin; {'D0', ..., 'A8'},</li>
   *           <li>{@code keys[3]} &isin; {'Eb0', ..., 'Bb8'},</li>
   *           <li>{@code keys[4]} &isin; {'E0', ..., 'B8'}, and</li>
   *           <li>{@code keys[5]} &isin; {'F0', ..., 'C9'}</li>
   *         </ul>
   *         <span style="margin-left:1em">{@code keys[0]}&lt; {@code
   *         keys[1]}&lt; {@code keys[2]}&lt;{@code keys[3]}&lt; {@code
   *         keys[4]}&lt; {@code keys[5]} must hold.</span>
   * @param levels
   *          An array of exactly six levels, each &isin; [0,99]
   * @throws IllegalArgumentException if
   *          <ul style="margin-bottom:0">
   *            <li>{@code module} is out of range, or</li>
   *            <li>{@code keys} or {@code levels} is {@code null} or does not
   *              contain exactly six values, or</li>
   *            <li>any value in {@code keys} or {@code levels} is not
   *              permissible</li>
   * @see VZ-1 User Manual, Menu 1-11, p. 44
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 12
   */
  public void setAmpKf(int module, String[] keys, int[] levels)
  {
    // Sanity checks
    if (module<VZ1.M1 || module>VZ1.M8)
      throw SYX.IllArgExc(VZ1.E_BAD_M,module,VZ1.M1,VZ1.M8);

    // Invoke internal setter
    String UIDpfx = UID_Mm_AMP_KF_Pp_KEY(module,VZ1.P1).replace(".P1.KEY","");
    int_setKf(UIDpfx,keys,levels);
    // NOTE: updateChecksum() invoked by int_setKf(...) 
  }

  // -  VZ-Menu 1-12: VELOCITY LEVEL (DCA)  - - - - - - - - - - - - - - - - - -

  /**
   * Gets the amplitude velocity sensitivity curve of a module.
   *
   * @param module
   *          The module &isin; {{@link VZ1#M1}, ..., {@link VZ1#M8}}
   * @return The amplitude velocity sensitivity curve &isin; {{@link VZ1#C1},
   *         ... {@link VZ1#C8}}
   * @throws IllegalArgumentException if the argument is out of range
   * 
   * @see VZ-1 User Manual, Menu 1-12, p. 45
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 13
   */
  public int getAmpVelSensCurve(int module)
  throws IllegalArgumentException
  {
    return getMidiValue(UID_Mm_AMP_VELLEVEL_CURVE(module));
  }

  /**
   * Gets the amplitude velocity sensitivity of a module.
   *
   * @param module
   *          The module &isin; {{@link VZ1#M1}, ..., {@link VZ1#M8}}
   * @return The amplitude velocity sensitivity &isin; [0,31]
   * @throws IllegalArgumentException if the argument is out of range
   * 
   * @see VZ-1 User Manual, Menu 1-12, p. 45
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 13
   */
  public int getAmpVelSens(int module)
  throws IllegalArgumentException
  {
    return getModelValue(UID_Mm_AMP_VELLEVEL_SENSITIVITY(module));
  }

  /**
   * Sets the amplitude velocity sensitivity of a module.
   * 
   * @param module
   *          The module &isin; {{@link VZ1#M1}, ..., {@link VZ1#M8}}
   * @param curve
   *          The amplitude velocity sensitivity curve &isin; {{@link VZ1#C1},
   *          ... {@link VZ1#C8}}
   * @param sens
   *          The amplitude velocity sensitivity &isin; [0,31]
   * @throws IllegalArgumentException if any argument is out of range
   * 
   * @see VZ-1 User Manual, Menu 1-12, p. 45
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 13
   */
  public void setAmpVelSens(int module, int curve, int sens)
  throws IllegalArgumentException
  {
    try
    {
      setMidiValue (UID_Mm_AMP_VELLEVEL_CURVE      (module),curve);
      setModelValue(UID_Mm_AMP_VELLEVEL_SENSITIVITY(module),sens );
    }
    catch (InvalidMidiDataException e)
    { // One of the arguments is illegal, hence...
      throw new IllegalArgumentException(e);
    }
    finally
    {
      updateChecksum();
    }
  }

  // -  VZ-Menu 1-13: TREMOLO - - - - - - - - - - - - - - - - - - - - - - - - -

  /**
   * Returns the tremolo multi flag.
   * 
   * @return {@code true} if the tremolo effect is triggered separately or every
   *         note played, {@code false} if the effect is synchronized over all
   *         simultaneously sounding notes. 
   * 
   * @see VZ-1 User Manual, Menu 1-13, p. 46
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 13
   */
  public boolean isTremoloMulti()
  {
    return getModelValueAsString(UID_TREMOLO_MULTI)=="ON";
  }

  /**
   * Returns the tremolo waveform &isin; {{@link VZ1#TRIANG}, {@link VZ1#SAWUP}, 
   * {@link VZ1#SAWDN}, {@link VZ1#SQUARE}}.
   * 
   * @see VZ-1 User Manual, Menu 1-13, p. 46
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 13
   */
  public int getTremoloWave()
  {
    return getMidiValue(UID_TREMOLO_WAVE);
  }

  /**
   * Returns the tremolo effect depth &isin; [0,99].
   * 
   * @see VZ-1 User Manual, Menu 1-13, p. 46
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 14
   */
  public int getTremoloDepth()
  {
    return getModelValue(UID_TREMOLO_DEPTH);
  }

  /**
   * Returns the tremolo effect rate &isin; [0,99].
   * 
   * @see VZ-1 User Manual, Menu 1-13, p. 46
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 14
   */
  public int getTremoloRate()
  {
    return getModelValue(UID_TREMOLO_RATE);
  }

  /**
   * Returns the tremolo effect delay &isin; [0,99]
   * 
   * @see VZ-1 User Manual, Menu 1-13, p. 46
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 14
   */
  public int getTremoloDelay()
  {
    return getModelValue(UID_TREMOLO_DELAY);
  }

  /**
   * Sets the tremolo effect parameters.
   * 
   * @param multi
   *          The tremolo multi flag, {@code true} if the vibrato effect is 
   *          triggered separately or every note played, {@code false} if the 
   *          effect is synchronized over all simultaneously sounding notes
   * @param wave
   *          The tremolo waveform &isin; {{@link VZ1#TRIANG}, {@link
   *          VZ1#SAWUP}, {@link VZ1#SAWDN}, {@link VZ1#SQUARE}}
   * @param depth
   *          The tremolo effect depth &isin; [0,99]
   * @param rate
   *          The tremolo effect rate &isin; [0,99]
   * @param delay
   *          The tremolo effect delay &isin; [0,99]
   * @throws IllegalArgumentException if any argument is out of range
   * 
   * @see VZ-1 User Manual, Menu 1-13, p. 46
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 13f
   */
  public void setTremolo(boolean multi, int wave, int depth, int rate, int delay)
  throws IllegalArgumentException
  {
    try
    {
      setModelValue(UID_TREMOLO_MULTI,multi?"ON":"OFF");
      setMidiValue (UID_TREMOLO_WAVE ,wave            );
      setModelValue(UID_TREMOLO_DEPTH,depth           );
      setModelValue(UID_TREMOLO_RATE ,rate            );
      setModelValue(UID_TREMOLO_DELAY,delay           );
    }
    catch (InvalidMidiDataException e)
    { // One of the arguments is illegal, hence...
      throw new IllegalArgumentException(e);
    }
    finally
    {
      updateChecksum();
    }
  }

  // -  VZ-Menu 1-14: AMPLITUDE SENSITIVITY - - - - - - - - - - - - - - - - - -

  /**
   * Gets the amplitude effect sensitivity for a module.
   * 
   * @param module
   *          The module &isin; {{@link VZ1#M1}, ..., {@link VZ1#M8}}
   * @return The sensitivity &isin; [0,7]
   * @throws IllegalArgumentException
   *          if the argument is out of range
   * 
   * @see VZ-1 User Manual, Menu 1-14, p. 47
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 11
   */
  public int getAmpEffSensitivity(int module)
  {
    return getModelValue(UID_Mm_AMPSENS(module));
  }

  /**
   * Sets the amplitude effect sensitivity for a module.
   * 
   * @param module
   *          The module &isin; {{@link VZ1#M1}, ..., {@link VZ1#M8}}
   * @param value
   *          The sensitivity &isin; [0,7]
   * @throws IllegalArgumentException
   *          if either argument is out of range
   * 
   * @see VZ-1 User Manual, Menu 1-14, p. 47
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 11
   */
  public void setAmpEffSens(int module, int value)
  {
    try
    {
      setModelValue(UID_Mm_AMPSENS(module),value);
    }
    catch (InvalidMidiDataException e)
    { // One of the arguments is illegal, hence...
      throw new IllegalArgumentException(e);
    }
    finally
    {
      updateChecksum();
    }
  }

  // -  VZ-Menu 1-15: TOTAL LEVEL - - - - - - - - - - - - - - - - - - - - - - -

  /**
   * Gets the total level
   * 
   * @return The total level &isin; [0,99]
   * 
   * @see VZ-1 User Manual, Menu 1-10, p. 43
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 11
   */
  public int getTotelLevel()
  {
    return getModelValue(UID_TOTALLEVEL);
  }

  /**
   * Sets the total level
   * 
   * @param value
   *          The total level &isin; [0,99]
   *          
   * @see VZ-1 User Manual, Menu 1-10, p. 43
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 11
   */
  public void setTotalLevel(int value)
  throws IllegalArgumentException
  {
    try
    {
      setModelValue(UID_TOTALLEVEL,value);
    }
    catch (InvalidMidiDataException e)
    { // One of the arguments is illegal, hence...
      throw new IllegalArgumentException(e);
    }
    finally
    {
      updateChecksum();
    }
  }

  // -  VZ-Menu 1-16: KF RATE (DCO/DCA) - - - - - - - - - - - - - - - - - - - -

  /**
   * Gets an rate key-following key name (key on the keyboard) of a
   * key-following support point.
   * 
   * @param point
   *         The support point &isin; {{@link VZ1#P1}, ..., {@link VZ1#P6}}
   * @return The "display" data value &isin; {'C0', 'C#0', ..., 'C9'}
   * @throws IllegalArgumentException if the argument is out of range
   * 
   * @see VZ-1 User Manual, Menu 1-16, p. 49
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 12
   */
  public String getRateKfKey(int point)
  throws IllegalArgumentException
  {
    return getModelValueAsString(UID_RATE_KF_Pp_KEY(point));
  }

  /**
   * Gets an rate key-following rate of a key-following support point.
   * 
   * @param point
   *         The support point &isin; {{@link VZ1#P1}, ..., {@link VZ1#P6}}
   * @return The "display" data value &isin; [0,99]
   * @throws IllegalArgumentException if the argument is out of range
   * 
   * @see VZ-1 User Manual, Menu 1-16, p. 49
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 12
   */
  public int getRateKfRate(int point)
  throws IllegalArgumentException
  {
    return getModelValue(UID_RATE_KF_Pp_RATE(point));
  }

  /**
   * Gets the rate key-following key names (keys on the keyboard).
   * 
   * @return An array of exactly six key names &isin; {'C0', 'C#0', ..., 'C9'}
   * 
   * @see VZ-1 User Manual, Menu 1-16, p. 49
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 12
   */
  public String[] getRateKfKeys()
  {
    String keys[] = new String[6];
    for (int point=VZ1.P1; point<=VZ1.P6; point++)
      keys[point-VZ1.P1] = getRateKfKey(point);
    return keys;
  }

  /**
   * Gets the rate key-following rates.
   * 
   * @return An array of exactly rates &isin; [0,99]
   * 
   * @see VZ-1 User Manual, Menu 1-16, p. 49
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 12
   */
  public int[] getRateKfRates()
  {
    int levels[] = new int[6];
    for (int point=VZ1.P1; point<=VZ1.P6; point++)
      levels[point-VZ1.P1] = getRateKfRate(point);
    return levels;
  }

  /**
   * Sets the rate key-following parameters.
   * 
   * @param keys
   *         An array of exactly six key names in the following ranges
   *         <ul style="margin-bottom:0">
   *           <li>{@code keys[0]} &isin; {'C0', ..., 'G8'},</li>
   *           <li>{@code keys[1]} &isin; {'C#0', ..., 'Ab8'},</li>
   *           <li>{@code keys[2]} &isin; {'D0', ..., 'A8'},</li>
   *           <li>{@code keys[3]} &isin; {'Eb0', ..., 'Bb8'},</li>
   *           <li>{@code keys[4]} &isin; {'E0', ..., 'B8'}, and</li>
   *           <li>{@code keys[5]} &isin; {'F0', ..., 'C9'}</li>
   *         </ul>
   *         <span style="margin-left:1em">{@code keys[0]}&lt; {@code
   *         keys[1]}&lt; {@code keys[2]}&lt;{@code keys[3]}&lt; {@code
   *         keys[4]}&lt; {@code keys[5]} must hold.</span>
   * @param rates
   *          An array of exactly six rates, each &isin; [0,99]
   * @throws IllegalArgumentException if
   *          <ul style="margin-bottom:0">
   *            <li>{@code keys} or {@code levels} is {@code null} or does not
   *              contain exactly six values, or</li>
   *            <li>any value in {@code keys} or {@code levels} is not
   *              permissible</li>
   * @see VZ-1 User Manual, Menu 1-16, p. 49
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 11
   */
  public void setRateKf(String[] keys, int[] rates)
  {
    String UIDpfx = UID_RATE_KF_Pp_KEY(VZ1.P1).replace(".P1.KEY","");
    int_setKf(UIDpfx,keys,rates);
    // NOTE: updateChecksum() invoked by int_setKf(...) 
  }

  // -  VZ-Menu 1-17: VELOCITY RATE (DCO/DCA) - - - - - - - - - - - - - - - - -

  /**
   * Gets the pitch envelope velocity rate flag for a envelope step.
   *  
   * @param step
   *          The envelope step &isin; {{@link VZ1#S1}, ..., {@link VZ1#S8}}
   * @return  The pitch envelope velocity rate flag of {@code step}; {@code
   *          false} if inactive (display value "*") and {@code true} if active
   *          (display value "E") 
   * @throws IllegalArgumentException
   *          if the argument is out of range 
   * 
   * @see VZ-1 User Manual, Menu 1-17, p. 50
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 10
   */
  public boolean isPitchEnvStepVelRate(int step)
  throws IllegalArgumentException
  {
    return getModelValueAsString(UID_PITCH_ENV_Ss_VELRATE(step))=="E";
  }

  /**
   * Gets the amplitude envelope velocity rate flag for a module and an envelope
   * step.
   * 
   * @param module
   *          The module &isin; {{@link VZ1#M1}, ..., {@link VZ1#M8}}
   * @param step
   *          The envelope step &isin; {{@link VZ1#S1}, ..., {@link VZ1#S8}}
   * @return  The amplitude envelope velocity rate flag of module {@code module}
   *          and {@code step}; {@code false} if inactive (display value "*")
   *          and {@code true} if active (display value "E") 
   * @throws IllegalArgumentException
   *          if either argument is out of range 
   * 
   * @see VZ-1 User Manual, Menu 1-17, p. 50
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 10
   */
  public boolean isAmpEnvStepVelRate(int module, int step)
  throws IllegalArgumentException
  {
    return getModelValueAsString(UID_Mm_AMP_ENV_Ss_VELRATE(module,step))=="E";
  }

  /**
   * Sets the pitch envelope velocity rate flag for a pitch envelope step.
   * 
   * @param step
   *          The envelope step &isin; {{@link VZ1#S1}, ..., {@link VZ1#S8}}
   * @param vrate
   *          The pitch envelope velocity rate flag of {@code step}; {@code
   *          false} to deactivate (display value "*") and {@code true} to
   *          activate (display value "E")
   * @throws IllegalArgumentException
   *          if argument {@code step} is out of range 
   * 
   * @see VZ-1 User Manual, Menu 1-17, p. 50
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 10
   */
  public void setPitchEnvStepVelRate(int step, boolean vrate)
  throws IllegalArgumentException
  {
    try
    {
      setModelValue(UID_PITCH_ENV_Ss_VELRATE(step),vrate?"E":"*");
    }
    catch (InvalidMidiDataException e)
    { // One of the arguments is illegal, hence...
      throw new IllegalArgumentException(e);
    }
    finally
    {
      updateChecksum();
    }
  }

  /**
   * Sets the amplitude envelope velocity rate flag for a module and an envelope
   * step.
   * 
   * @param module
   *          The module &isin; {{@link VZ1#M1}, ..., {@link VZ1#M8}}
   * @param step
   *          The envelope step &isin; {{@link VZ1#S1}, ..., {@link VZ1#S8}}
   * @param vrate
   *          The amplitude envelope velocity rate flag of module {@code module}
   *          and {@code step}; {@code false} to deactivate (display value "*")
   *          and {@code true} to activate (display value "E") 
   * @throws IllegalArgumentException
   *          if either argument is out of range 
   * 
   * @see VZ-1 User Manual, Menu 1-17, p. 50
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 10
   */
  public void setAmpEnvStepVelRate(int module, int step, boolean vrate)
  {
    try
    {
      setModelValue(UID_Mm_AMP_ENV_Ss_VELRATE(module,step),vrate?"E":"*");
    }
    catch (InvalidMidiDataException e)
    { // One of the arguments is illegal, hence...
      throw new IllegalArgumentException(e);
    }
    finally
    {
      updateChecksum();
    }
  }

  /**
   * Gets the rate velocity sensitivity curve.
   *
   * @return The rate velocity sensitivity curve &isin; {{@link VZ1#C1}, ... 
   *          {@link VZ1#C8}}
   * @throws IllegalArgumentException if the argument is out of range
   * 
   * @see VZ-1 User Manual, Menu 1-17, p. 50
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 13
   */
  public int getRateVelSensCurve()
  throws IllegalArgumentException
  {
    return getMidiValue(UID_RATE_VELLEVEL_CURVE);
  }

  /**
   * Gets the rate velocity sensitivity.
   *
   * @return The rate velocity sensitivity &isin; [0,31]
   * 
   * @see VZ-1 User Manual, Menu 1-17, p. 50
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 13
   */
  public int getRateVelSens()
  throws IllegalArgumentException
  {
    return getModelValue(UID_RATE_VELLEVEL_SENSITIVITY);
  }

  /**
   * Sets the rate velocity sensitivity of a module.
   * 
   * @param curve
   *          The rate velocity sensitivity curve &isin; {{@link VZ1#C1}, ... 
   *          {@link VZ1#C8}}
   * @param sens
   *          The rate velocity sensitivity &isin; [0,31]
   * @throws IllegalArgumentException if any argument is out of range
   * 
   * @see VZ-1 User Manual, Menu 1-17, p. 50
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 13
   */
  public void setRateVelSens(int curve, int sens)
  throws IllegalArgumentException
  {
    try
    {
      setMidiValue (UID_RATE_VELLEVEL_CURVE      ,curve);
      setModelValue(UID_RATE_VELLEVEL_SENSITIVITY,sens );
    }
    catch (InvalidMidiDataException e)
    { // One of the arguments is illegal, hence...
      throw new IllegalArgumentException(e);
    }
    finally
    {
      updateChecksum();
    }
  }

  // -  VZ-Menu 1-18: VOICE NAME  - - - - - - - - - - - - - - - - - - - - - - -

  /**
   * Returns the voice name.
   * 
   * @see VZ-1 User Manual, Menu 1-18, p. 51
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 14
   */
  @Override
  public String getName()
  {
    try
    {
      return readString(UIDpfx_NAME);
    }
    catch (Throwable e)
    { // Cannot happen
      throw SYX.InternalError(e);
    }
  }

  /**
   * Sets the voice name.
   * 
   * @param name
   *          The new voice name. Will be truncated if longer that 12 
   *          characters. May be {@code null} to set empty name.
   * 
   * @see VZ-1 User Manual, Menu 1-18, p. 51
   * @see VZ-1/VZ-10M MIDI System Exclusive Specification, p. 14
   */
  @Override
  public void setName(String name)
  throws IllegalArgumentException
  {
    try
    {
      writeString(UIDpfx_NAME,name,true);
    }
    catch (InvalidMidiDataException e)
    { // One of the arguments is illegal, hence...
      throw new IllegalArgumentException(e);
    }
    finally
    {
      updateChecksum();
    }
  }

  // -- Pretty Printing -------------------------------------------------------

  /**
   * Prints a string with prefix and colon.
   * 
   * @param prefix
   *          Line prefix
   * @param format
   *          Format string
   * @param args
   *          Arguments
   * @return The string
   */
  private static String P(String prefix, String format, Object... args)
  {
    return String.format("%-15s: %s",prefix,String.format(format,args));
  }

  /**
   * Prints a string with prefix and <i>without</i> colon.
   * 
   * @param prefix
   *          Line prefix
   * @param format
   *          Format string
   * @param args
   *          Arguments
   * @return The string
   */
  private static String p(String prefix, String format, Object... args)
  {
    return String.format("%-15s  %s",prefix,String.format(format,args));
  }

  /**
   * Prints a string.
   * 
   * @param format
   *          Format string
   * @param args
   *          Arguments
   * @return The string
   */
  private static String S(String format, Object... args)
  {
    return String.format(format,args);
  }

  /**
   * Pretty-prints this Tone Data model into a string.
   * 
   * @return The printed string
   */
  public String prettyPrintModel()
  {
    String s = "-".repeat(89)+"\n";
    String t;

    // General Information
    s += S("TONE DATA MODEL \"%s\"\n",getName());
    s += "-".repeat(89)+"\n";
    s += P("Total Level","%2d\n",getTotelLevel());
    s += P("Octave",getOctave()==0?"%2d\n":"%+2d\n",getOctave());
    
    try
    {
      t = readString(UIDpfx_UNDOCUMENTED);
      s += P("(Part 44)","\"%s\"\n",t);
    }
    catch (Exception e)
    { // Cannot happen
      throw new Error(e);
    }

    // Algorithm
    s += p("","      LINE A        LINE B        LINE C        LINE D\n");
    s += P("Algorithm","      ");
    for (int m=VZ1.A; m<=VZ1.D; m++)
    {
      boolean lineOn = isModuleOn(2*m) || isModuleOn(2*m+1);
      if (m>VZ1.A)
        s += S("  %-5s  ",isExtPhase(m) && lineOn ? "-eP->" : "");
      t = " ???";
      switch (getLineAlgorithm(m))
      {
      case VZ1.MIX  : t = " MIX" ; break;
      case VZ1.RING : t = " RING"; break;
      case VZ1.PHASE: t = "PHASE"; break;
      }
      s += S("%-5s",lineOn ? t : "");
    }
    s += S("\n");

    // Module Settings
    s += p("","    M1     M2     M3     M4     M5     M6     M7     M8"
             + "  PITCH   RATE\n");

    // -- Waveform
    s += P("Waveform","");
    for (int m=VZ1.M1; m<=VZ1.M8; m++)
      s += S("%6s ",isModuleOn(m) ?VZ1. VN_WFORM[getWaveform(m)] : "--");
    s += S("\n");

    // -- Detune
    s += P("Detune","");
    for (int m=VZ1.M1; m<=VZ1.M8; m++)
    {
      t = getDetunePolarity(m)>0 ? "+" : "-";
      t += S("%d:%d",getDetuneOctave(m),getDetuneNote(m));
      s += S("%6s ",isModuleOn(m) ? t : "");
    }
    s += S("\n");
    s += P("- Fine","");
    for (int m=VZ1.M1; m<=VZ1.M8; m++)
      s += isModuleOn(m) ? S("%6d ",getDetuneFine(m)) : "       ";
    s += S("\n");
    s += P("- Pitch Fix","");
    for (int m=VZ1.M1; m<=VZ1.M8; m++)
    {
      t = "-";
      switch (getDetuneMode(m))
      {
      case VZ1.FIXHI: t = "1";    break;
      case VZ1.FIXLO: t = "1/16"; break;
      }
      s += S("%6s ",isModuleOn(m) ? t : "");
    }
    s += S("\n");

    // -- DCO/DCA Envelope Depth
    s += P("Envelope Depth","");
    for (int m=VZ1.M1; m<=VZ1.M8; m++)
      s += S("%6s ",isModuleOn(m) ? getAmpEnvDepth(m) : "");
    s += S("%5s%s",getPitchEnvDepth(),getPitchEnvRange()==0?"N":"W");
    s += S("\n");

    // -- Amplitude Effect Sensitivity
    s += P("Amp.Sensitivity","");
    for (int m=VZ1.M1; m<=VZ1.M8; m++)
      s += S("%6s ",isModuleOn(m) ? getAmpEffSensitivity(m) : "");
    s += S("\n");

    // -- Velocity Sensitivity
    s += P("Vel.Sensitivity","");
    for (int m=VZ1.M1; m<=VZ1.M8; m++)
    {
      t  = S("C%d:",getAmpVelSensCurve(m)+1);
      t += S("%2d" ,getAmpVelSens(m)); 
      s += S("%6s ",isModuleOn(m)?t:"");
    }
    t  = S("C%d:",getPitchVelSensCurve()+1);
    t += S("%2d" ,getPitchVelSens()); 
    s += S("%6s ",t);
    t  = S("C%d:",getRateVelSensCurve()+1);
    t += S("%2d" ,getRateVelSens()); 
    s += S("%6s ",t);
    s += S("\n");

    // -- DCO Envelope
    s += S("\n");
    s += p("Pitch Envelope","");
    for (int step=0; step<=7; step++)
      s += S("STEP %d ",step+1);
    s += S("\n");
    s += P("- Rate, E/*","");
    for (int step=0; step<=7; step++)
    {
      s += S("%3d",getPitchEnvStepRate(step));
      s += isPitchEnvStepVelRate(step) ? "  E " : "  * ";
      if (getPitchEnvEndStep()==step)
        break;
    }
    s += S("\n");
    s += P("- Level, SS","");
    for (int step=0; step<=7; step++)
    {
      s += S(getPitchEnvStepLevel(step)==0?"%3d ":"%+3d ",getPitchEnvStepLevel(step));
      s += getPitchEnvSustainStep()==step ? "SS " : "   ";
      if (getPitchEnvEndStep()==step)
        break;
    }

    // -- DCA Envelope
    s += S("\n");
    s += S("\n");
    s += p("Amp. Envelope","");
    for (int step=0; step<=7; step++)
      s += S("STEP %d ",step+1);
    for (int m=VZ1.M1; m<=VZ1.M8; m++)
    {
      s += S("\n");
      t  = S("- M%d Rate, E/*",m+1);
      s += P(t,"");
      for (int step=0; step<=7; step++)
      {
        s += S("%3d",getAmpEnvStepRate(m,step));
        s += isAmpEnvStepVelRate(m,step) ? "  E " : "  * ";
        if (getAmpEnvEndStep(m)==step)
          break;
      }
      s += S("\n");
      s += P("     Level, SS","");
      for (int step=0; step<=7; step++)
      {
        s += S("%3d ",getAmpEnvStepLevel(m,step));
        s += getAmpEnvSustainStep(m)==step ? "SS " : "   ";
        if (getAmpEnvEndStep(m)==step)
          break;
      }
    }
    s += S("\n\n");

    s += p("Key Following","");
    for (int point=VZ1.P1; point<=VZ1.P6; point++)
      s += S("POINT%d ",point+1);
    s += S("\n");
    for (int me=VZ1.M1; me<=VZ1.M8; me++)
    {
      s += P(String.format("- %-5s Key","M"+(me+1)),"");
      for (int point=VZ1.P1; point<=VZ1.P6; point++)
        s += S("%6s ",getAmpKfKey(me,point));
      s += S("\n");
      s += P("        Level","");
      for (int point=VZ1.P1; point<=VZ1.P6; point++)
        s += S("%6s ",getAmpKfLevel(me,point));
      s += S("\n");
    }
    s += P("- Pitch Key","");
    for (int point=VZ1.P1; point<=VZ1.P6; point++)
      s += S("%6s ",getPitchKfKey(point));
    s += S("\n");
    s += P("        Level","");
    for (int point=VZ1.P1; point<=VZ1.P6; point++)
      s += S("%6s ",getPitchKfLevel(point));
    s += S("\n");
    s += P("- Rate  Key","");
    for (int point=VZ1.P1; point<=VZ1.P6; point++)
      s += S("%6s ",getRateKfKey(point));
    s += S("\n");
    s += P("        Rate ","");
    for (int point=VZ1.P1; point<=VZ1.P6; point++)
      s += S("%6s ",getRateKfRate(point));
    s += S("\n\n");

    // Vibrato & Tremolo
    s += p("Effects","Multi   Wave  Depth   Rate  Delay\n");
    s += P("- Vibrato","");
    s += S("%5s ",isVibratoMulti()?"yes":"no");
    t = "???";
    switch (getVibratoWave())
    {
    case VZ1.TRIANG: t = "TRIANG"; break;
    case VZ1.SAWUP : t = "SAW_UP"; break;
    case VZ1.SAWDN : t = "SAW_DN"; break;
    case VZ1.SQUARE: t = "SQUARE"; break;
    }
    s += S("%6s  ",t);
    s += S("   %2d   ",getVibratoDepth());
    s += S("  %2d   " ,getVibratoRate());
    s += S("  %2d   " ,getVibratoDelay());
    s += S("\n");
    s += P("- Tremolo","");
    s += S("%5s ",isTremoloMulti()?"yes":"no");
    t = "???";
    switch (getTremoloWave())
    {
    case VZ1.TRIANG: t = "TRIANG"; break;
    case VZ1.SAWUP : t = "SAW_UP"; break;
    case VZ1.SAWDN : t = "SAW_DN"; break;
    case VZ1.SQUARE: t = "SQUARE"; break;
    }
    s += S("%6s  ",t);
    s += S("   %2d   ",getTremoloDepth());
    s += S("  %2d   " ,getTremoloRate());
    s += S("  %2d   " ,getTremoloDelay());
    s += S("\n");

    // -- The End
    return s + "-".repeat(89)+"\n";
  }

  // -- Workers ---------------------------------------------------------------

  /**
   * Sets key-following parameters.
   * 
   * @param UIDpfx
   *          Prefix of the UIDs of the parameters to set, one of the following:
   *          <ul style="margin-bottom:0">
   *            <li>'{@code M<m>.AMP.KF}', where {@code <m>} &isin; [1,8],</li>
   *            <li>'{@code RATE.KF}', or</li>
   *            <li>'{@code PITCH.KF}'</li>
   *          </ul>
   * @param keys
   *         An array of exactly six key names in the following ranges
   *         <ul style="margin-bottom:0">
   *           <li>{@code keys[0]} &isin; {'C0', ..., 'G8'},</li>
   *           <li>{@code keys[1]} &isin; {'C#0', ..., 'Ab8'},</li>
   *           <li>{@code keys[2]} &isin; {'D0', ..., 'A8'},</li>
   *           <li>{@code keys[3]} &isin; {'Eb0', ..., 'Bb8'},</li>
   *           <li>{@code keys[4]} &isin; {'E0', ..., 'B8'}, and</li>
   *           <li>{@code keys[5]} &isin; {'F0', ..., 'C9'}</li>
   *         </ul>
   *         <span style="margin-left: 1em"> {@code keys[0]}&lt; {@code
   *         keys[1]}&lt; {@code keys[2]}&lt; {@code keys[3]}&lt; {@code
   *         keys[4]}&lt; {@code keys[5]} must hold.</span>
   * @param levels
   *          An array of exactly six key following levels, each in the
   *          following ranges:
   *          <ul>
   *            <li>[0,99] if {@code UIDpfx} is '{@code M<m>.AMP.KF}',</li>
   *            <li>[0,63] if {@code UIDpfx} is '{@code RATE.KF}', and</li>
   *            <li>[0,99] if {@code UIDpfx} is '{@code PITCH.KF}'</li>
   *          </ul>
   * @throws IllegalArgumentException if
   *           <ul style="margin-bottom:0">
   *             <li>{@code keys} or {@code levels} is {@code null} or does not
   *               contain exactly six values, or</li>
   *             <li>any value in {@code keys} or {@code levels} is not
   *               permissible</li>
   *           </ul>
   */
  private void int_setKf(String UIDpfx, String[] keys, int[] levels)
  throws IllegalArgumentException
  {
    // Sanity checks
    // - Check argument UIDpfx
    String[] UIDpfxs = new String[]
    { // Permissible UID prefixes
        UID_Mm_AMP_KF_Pp_KEY(VZ1.M1,VZ1.P1).replace(".P1.KEY",""),
        UID_Mm_AMP_KF_Pp_KEY(VZ1.M2,VZ1.P1).replace(".P1.KEY",""),
        UID_Mm_AMP_KF_Pp_KEY(VZ1.M3,VZ1.P1).replace(".P1.KEY",""),
        UID_Mm_AMP_KF_Pp_KEY(VZ1.M4,VZ1.P1).replace(".P1.KEY",""),
        UID_Mm_AMP_KF_Pp_KEY(VZ1.M5,VZ1.P1).replace(".P1.KEY",""),
        UID_Mm_AMP_KF_Pp_KEY(VZ1.M6,VZ1.P1).replace(".P1.KEY",""),
        UID_Mm_AMP_KF_Pp_KEY(VZ1.M7,VZ1.P1).replace(".P1.KEY",""),
        UID_Mm_AMP_KF_Pp_KEY(VZ1.M8,VZ1.P1).replace(".P1.KEY",""),
        UID_PITCH_KF_Pp_KEY (       VZ1.P1).replace(".P1.KEY",""),
        UID_RATE_KF_Pp_KEY  (       VZ1.P1).replace(".P1.KEY","")
    };
    boolean UIDpfxFound = false;
    for (String s : UIDpfxs)
      if (s.equals(UIDpfx))
      {
        UIDpfxFound = true;
        break;
      }
    if (!UIDpfxFound)
      throw SYX.InternalError("Invalid UID prefix '"+UIDpfx+"'");

    // - Check arguments keys and levels
    if (keys==null || keys.length!=6)
      SYX.IllArgExc(SYX.E_ARG_BADLEN,"keys",6);
    if (levels==null || levels.length!=6)
      SYX.IllArgExc(SYX.E_ARG_BADLEN,"levels",6);

    // Set parameters
    String UIDsfx = ".LEV";
    if (UIDpfx.equals(UID_RATE_KF_Pp_KEY(VZ1.P1).replace(".P1.KEY","")))
      UIDsfx = ".RATE";
    try
    {
      // Step 1: Set key parameters to maximums (avoids validation errors)
      for (int p=VZ1.P6; p>=VZ1.P1; p--)
      {
        String  UID_KEY = UIDpfx+"."+int_UID_Pp(p)+".KEY";
        SyxParamInfo pi = getParamInfo(UID_KEY);
        int maxMidiValue = pi.getMidiValueRange()[1];
        setMidiValue(UID_KEY,maxMidiValue);
      }
  
      // Step 2: Set key and level parameters
      for (int p=VZ1.P1; p<=VZ1.P6; p++)
      {
        String UID_KEY = UIDpfx+"."+int_UID_Pp(p)+".KEY";
        String UID_LEV = UIDpfx+"."+int_UID_Pp(p)+UIDsfx;
        setModelValue(UID_KEY,keys  [p]);
        setModelValue(UID_LEV,levels[p]);
      }
    }
    catch (Exception e)
    {
      throw new IllegalArgumentException(e);
    }
    finally
    {
      updateChecksum();
    }
  }

}

// EOF