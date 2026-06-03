/**
 * Device model of the Casio VZ-1/10M synthesizers
 * 
 * <p><b>Comparison of VZ Combination and Multi Modes:</b></p>
 * <table style="margin-top:1em; margin-left:2em; border-collapse:collapse;">
 *   <tr style="border-top: solid thin; border-bottom: solid thin;">
 *     <td><b>Feature</b></td>
 *     <td><b>Combi&nbsp;</b></td>
 *     <td><b>Multi</b></td>
 *   </tr>
 *   <tr>
 *     <td>Name and store settings at VZ</td>
 *     <td style="text-align:center">&checkmark;</td>
 *     <td style="text-align:center">&ndash;</td>
 *   </tr>
 *   <tr>
 *     <td>Maximal # of parts<sup>1</sup></td>
 *     <td style="text-align:center">4</td>
 *     <td style="text-align:center">8</td>
 *   </tr>
 *   <tr>
 *     <td>Select part patch via SysEx</td>
 *     <td style="text-align:center">&checkmark;</td>
 *     <td style="text-align:center">&checkmark;</td>
 *   </tr>
 *   <tr">
 *     <td>Download part patches via SysEx</td>
 *     <td style="text-align:center">&checkmark;(?)</td>
 *     <td style="text-align:center">&ndash;</td>
 *   </tr>
 *   <tr>
 *     <td>Activate/deactivate parts via SysEx</td>
 *     <td style="text-align:center">(&checkmark;)<sup>2</sup</td>
 *     <td style="text-align:center">&checkmark;<sup>3</sup></td>
 *   </tr>
 *   <tr>
 *     <td>Part MIDI channel assignable via SysEx</td>
 *     <td style="text-align:center">&ndash;</td>
 *     <td style="text-align:center">&checkmark;</td>
 *   </tr>
 *   <tr>
 *     <td>Part polyphony assignable via SysEx</td>
 *     <td style="text-align:center">&ndash;</td>
 *     <td style="text-align:center">&checkmark;</td>
 *   </tr>
 *   <tr>
 *     <td>Set part volume via SysEx</td>
 *     <td style="text-align:center">&checkmark;</td>
 *     <td style="text-align:center">&checkmark;</td>
 *   </tr>
 *   <tr>
 *     <td>Set part detune via SysEx</td>
 *     <td style="text-align:center">&checkmark;</td>
 *     <td style="text-align:center">&checkmark;</td>
 *   </tr>
 *   <tr style="border-bottom: solid thin;">
 *     <td>Set part panning via SysEx</td>
 *     <td style="text-align:center">&ndash;<sup>&hairsp;4</sup></td>
 *     <td style="text-align:center">&ndash;<sup>&hairsp;4</sup></td>
 *   </tr>
 * </table>
 * <table style="margin-top: 0.5em; margin-left:2em">
 *   <tr>
 *     <td style="vertical-align: top"><sup>1</sup></td>
 *     <td>"part" refers to a "sound" in VZ combination mode and to an "area" in
 *       VZ multi mode</td>
 *   </tr>
 *   <tr>
 *     <td style="vertical-align: top"><sup>2</sup></td>
 *     <td>limited, through combination mode</td>
 *   </tr>
 *   <tr>
 *     <td style="vertical-align: top"><sup>3</sup></td>
 *     <td>by setting polyphony to zero</td>
 *   </tr>
 *   <tr>
 *     <td style="vertical-align: top"><sup>4</sup></td>
 *     <td>panning not supported by VZ</td>
 *   </tr>
 * </table>

 * <p style="margin-bottom:0"><b>Emulating VZ Combination Mode in Multi Mode:</b></p>
 * <p style="margin-top:0.2em">
 *   Combination mode settings with patches P1, P2, P3, P4 and MIDI channels
 *   C1, C2, C3, C4 may be emulated in Multi mode as follows:
 * </p>
 * <table style="margin-top:1em; margin-left:2em; border-collapse:collapse;">
 *   <tr style="border-top: solid thin; border-bottom: solid thin;">
 *     <td style="text-align:center">&darr; Multi \ Combi&rarr;</td>
 *     <td style="text-align:center">1+2</td>
 *     <td style="text-align:center">3+4</td>
 *     <td style="text-align:center">1+2+3+4</td>
 *     <td style="text-align:center">1/3</td>
 *     <td style="text-align:center">1/3+4</td>
 *     <td style="text-align:center">1+2/3</td>
 *     <td style="text-align:center">1+2/3+4</td>
 *     <td style="text-align:center">1/2/3/4</td>
 *     <td style="text-align:center">(Line Out)</td>
 *   </tr>
 *   <tr>
 *     <td><b>Area 1</b></td>
 *     <td style="text-align:center">P1</td>
 *     <td style="text-align:center">P3</td>
 *     <td style="text-align:center">P1</td>
 *     <td style="text-align:center">P1</td>
 *     <td style="text-align:center">P1</td>
 *     <td style="text-align:center">P1</td>
 *     <td style="text-align:center">P1</td>
 *     <td style="text-align:center">P1</td>
 *     <td style="text-align:center">&rarr;1</td>
 *   </tr>
 *   <tr>
 *     <td>- Polyphony</td>
 *     <td style="text-align:center">8</td>
 *     <td style="text-align:center">8</td>
 *     <td style="text-align:center">4</td>
 *     <td style="text-align:center">8</td>
 *     <td style="text-align:center">8</td>
 *     <td style="text-align:center">4</td>
 *     <td style="text-align:center">4</td>
 *     <td style="text-align:center">4</td>
 *     <td style="text-align:center"></td>
 *   </tr>
 *   <tr>
 *     <td>- MIDI Channel</td>
 *     <td style="text-align:center">C1</td>
 *     <td style="text-align:center">C3</td>
 *     <td style="text-align:center">C1</td>
 *     <td style="text-align:center">C1</td>
 *     <td style="text-align:center">C1</td>
 *     <td style="text-align:center">C1</td>
 *     <td style="text-align:center">C1</td>
 *     <td style="text-align:center">C1</td>
 *     <td style="text-align:center"></td>
 *   </tr>
 *   <tr>
 *     <td><b>Area 2</b></td>
 *     <td style="text-align:center"></td>
 *     <td style="text-align:center"></td>
 *     <td style="text-align:center">P2</td>
 *     <td style="text-align:center"></td>
 *     <td style="text-align:center"></td>
 *     <td style="text-align:center">P2</td>
 *     <td style="text-align:center">P2</td>
 *     <td style="text-align:center">P2</td>
 *     <td style="text-align:center">&rarr;1</td>
 *   </tr>
 *   <tr>
 *     <td>- Polyphony<sup>1</sup></td>
 *     <td style="text-align:center"></td>
 *     <td style="text-align:center"></td>
 *     <td style="text-align:center">4</td>
 *     <td style="text-align:center"></td>
 *     <td style="text-align:center"></td>
 *     <td style="text-align:center">4</td>
 *     <td style="text-align:center">4</td>
 *     <td style="text-align:center">4</td>
 *     <td style="text-align:center"></td>
 *   </tr>
 *   <tr>
 *     <td>- MIDI Channel</td>
 *     <td style="text-align:center"></td>
 *     <td style="text-align:center"></td>
 *     <td style="text-align:center">C1</td>
 *     <td style="text-align:center"></td>
 *     <td style="text-align:center"></td>
 *     <td style="text-align:center">C1</td>
 *     <td style="text-align:center">C1</td>
 *     <td style="text-align:center">C2</td>
 *     <td style="text-align:center"></td>
 *   </tr>
 *   <tr>
 *     <td>Area 3<sup>1</sup></td>
 *     <td style="text-align:center">(not</td>
 *     <td style="text-align:center">used)</td>
 *     <td style="text-align:center"></td>
 *     <td style="text-align:center"></td>
 *     <td style="text-align:center"></td>
 *     <td style="text-align:center"></td>
 *     <td style="text-align:center"></td>
 *     <td style="text-align:center"></td>
 *     <td style="text-align:center"></td>
 *   </tr>
 *   <tr>
 *     <td>Area 4<sup>1</sup></td>
 *     <td style="text-align:center">(not</td>
 *     <td style="text-align:center">used)</td>
 *     <td style="text-align:center"></td>
 *     <td style="text-align:center"></td>
 *     <td style="text-align:center"></td>
 *     <td style="text-align:center"></td>
 *     <td style="text-align:center"></td>
 *     <td style="text-align:center"></td>
 *     <td style="text-align:center"></td>
 *   </tr>
 *   <tr>
 *     <td><b>Area 5</b></td>
 *     <td style="text-align:center">P2</td>
 *     <td style="text-align:center">P4</td>
 *     <td style="text-align:center">P3</td>
 *     <td style="text-align:center">P3</td>
 *     <td style="text-align:center">P3</td>
 *     <td style="text-align:center">P3</td>
 *     <td style="text-align:center">P3</td>
 *     <td style="text-align:center">P3</td>
 *     <td style="text-align:center">&rarr;2</td>
 *   </tr>
 *   <tr>
 *     <td>- Polyphony</td>
 *     <td style="text-align:center">8</td>
 *     <td style="text-align:center">8</td>
 *     <td style="text-align:center">4</td>
 *     <td style="text-align:center">8</td>
 *     <td style="text-align:center">4</td>
 *     <td style="text-align:center">8</td>
 *     <td style="text-align:center">4</td>
 *     <td style="text-align:center">4</td>
 *     <td style="text-align:center"></td>
 *   </tr>
 *   <tr>
 *     <td>- MIDI Channel</td>
 *     <td style="text-align:center">C1</td>
 *     <td style="text-align:center">C3</td>
 *     <td style="text-align:center">C1</td>
 *     <td style="text-align:center">C3</td>
 *     <td style="text-align:center">C3</td>
 *     <td style="text-align:center">C3</td>
 *     <td style="text-align:center">C3</td>
 *     <td style="text-align:center">C3</td>
 *     <td style="text-align:center"></td>
 *   </tr>
 *   <tr>
 *     <td><b>Area 6</b></td>
 *     <td style="text-align:center"></td>
 *     <td style="text-align:center"></td>
 *     <td style="text-align:center">P4</td>
 *     <td style="text-align:center"></td>
 *     <td style="text-align:center">P4</td>
 *     <td style="text-align:center"></td>
 *     <td style="text-align:center">P4</td>
 *     <td style="text-align:center">P4</td>
 *     <td style="text-align:center">&rarr;2</td>
 *   </tr>
 *   <tr>
 *     <td>- Polyphony<sup>1</sup></td>
 *     <td style="text-align:center"></td>
 *     <td style="text-align:center"></td>
 *     <td style="text-align:center">4</td>
 *     <td style="text-align:center"></td>
 *     <td style="text-align:center">4</td>
 *     <td style="text-align:center"></td>
 *     <td style="text-align:center">4</td>
 *     <td style="text-align:center">4</td>
 *     <td style="text-align:center"></td>
 *   </tr>
 *   <tr>
 *     <td>- MIDI Channel</td>
 *     <td style="text-align:center"></td>
 *     <td style="text-align:center"></td>
 *     <td style="text-align:center">C1</td>
 *     <td style="text-align:center"></td>
 *     <td style="text-align:center">C3</td>
 *     <td style="text-align:center"></td>
 *     <td style="text-align:center">C3</td>
 *     <td style="text-align:center">C4</td>
 *     <td style="text-align:center"></td>
 *   </tr>
 *   <tr>
 *     <td>Area 7<sup>1</sup></td>
 *     <td style="text-align:center">(not</td>
 *     <td style="text-align:center">used)</td>
 *     <td style="text-align:center"></td>
 *     <td style="text-align:center"></td>
 *     <td style="text-align:center"></td>
 *     <td style="text-align:center"></td>
 *     <td style="text-align:center"></td>
 *     <td style="text-align:center"></td>
 *     <td style="text-align:center"></td>
 *   </tr>
 *   <tr style="border-bottom: solid thin;">
 *     <td>Area 8<sup>1</sup></td>
 *     <td style="text-align:center">(not</td>
 *     <td style="text-align:center">used)</td>
 *     <td style="text-align:center"></td>
 *     <td style="text-align:center"></td>
 *     <td style="text-align:center"></td>
 *     <td style="text-align:center"></td>
 *     <td style="text-align:center"></td>
 *     <td style="text-align:center"></td>
 *     <td style="text-align:center"></td>
 *   </tr>
 * </table>
 * <table style="margin-top: 0.5em; margin-left:2em">
 *   <tr>
 *     <td style="vertical-align: top"><sup>1</sup></td>
 *     <td>Polyphony of unused areas must be set to zero.</td>
 *   </tr>
 * </table>
 * <p>In Multi mode it is not possible to download patches into "working areas"
 * of VZ. Hence, in Multi mode only patches stored in VZ can be used.
 * 
 * @see de.btu.kt.syx.devices.vz1.VZ1 VZ1
 * @see de.btu.kt.syx.devices.vz1.VZ1Patch VZ1Patch
 */
package de.btu.kt.syx.devices.vz1;

// EOF