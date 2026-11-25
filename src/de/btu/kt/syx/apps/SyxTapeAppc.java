package de.btu.kt.syx.apps;

import de.btu.kt.syx.util.AAppc;

/**
 * SysEx tape console app.
 * 
 * <p><b>Command Interface Draft:</b></p>
 * <pre> ------------------------------------------------------------------------------
 * General Commands
 * ------------------------------------------------------------------------------
 * ?          : Print this help
 * a=&lt;on|off&gt; : Auto-send model data after setting model parameter (default: off)
 * m          : List models on tape and available models
 * q          : Quit
 * x          : Debug command
 * &lt;MID&gt;      : Set current model
 *              &lt;MID&gt;: as listed by command 'm'
 *                      .            - root (no model, default)
 *                      D110#&lt;nn&gt;.MU - Roland D-110 Multi
 *                      K2k#&lt;nn&gt;.MU  - Kurzweil K2000 Multi
 *                      TG55#&lt;nn&gt;.MU - Yamaha TG55 Multi
 *                      VZ1#&lt;nn&gt;.CO  - Casio VZ-1 Combination
 *                      VZ1#&lt;nn&gt;.PA  - Casio VZ-1 Patch
 *                      ...
 *              &lt;nn&gt; : Device ID in {01,02,...,16}
 * +&lt;MID&gt;     : Add model to tape and fetch model data from synth
 * -&lt;MID&gt;     : Remove model from tape
 * ------------------------------------------------------------------------------
 * Model Commands (available only if current model is not '.')
 * ------------------------------------------------------------------------------
 * d          : Print model SysEx data
 * f          : Fetch model data from synth (1,2)
 * i          : Print model info
 * p          : Pretty-print model data
 * r          : Reset model (initialize model data with defaults)
 * s          : Send model data to synth (2)
 * *          : Show all model parameters
 * *=         : List permissible values for all model parameters
 * &lt;UID&gt;      : Show model parameter(s)
 * &lt;UID&gt;=     : List permissible values for model parameter(s)
 * &lt;UID&gt;=&lt;val&gt;: Set model parameter(s)
 *               &lt;UID&gt;: as listed by command '*', may contain wildcards:
 *                      *  matching any sequence of characters except '.'
 *                      ** matching any sequence of characters including '.'
 *               &lt;val&gt;: as listed by command '&lt;UID&gt;='
 * ------------------------------------------------------------------------------
 * File Commands
 * ------------------------------------------------------------------------------
 * pwd        : Print current directory
 * dir        : List contents of current directory
 * cd &lt;fname&gt; : Set current directory 
 *               &lt;fname&gt;: Directory name or
 *                        (&lt;nn&gt;) - Item number as listed by command 'dir'
 *                        ..     - Parent directory
 * rd &lt;fname&gt; : Read current model from SysEx file &lt;fname&gt; (2)
 * wr &lt;fname&gt; : Write current model to SysEx file &lt;fname&gt; (2)
 *               &lt;fname&gt;: File name or
 *                        (&lt;nn&gt;) - Item number as listed by command 'dir'
 * ------------------------------------------------------------------------------
 * (1) Model data are also read from SysEx input on MIDI interface if the
 *     respective model is on the tape
 * (2) If current model is '.', send/fetch/read/write all models on the tape
 * </pre>
 * 
 * @author Matthias Wolff
 */
public class SyxTapeAppc extends AAppc
// TODO: Implement SyxTapeAppc
{

  @Override
  protected void startup()
  {
    // TODO: Method stub: startup()
  }

  @Override
  protected void shutdown()
  {
    // TODO: Method stub: shutdown()
  }

  @Override
  protected String getAppName()
  {
    // TODO: Method stub: getAppName()
    return null;
  }

  @Override
  protected int getHelpPrefixLength()
  {
    // TODO: Method stub: getHelpPrefixLength()
    return 0;
  }

  public static void main(String[] args)
  {
    // TODO: Method stub: main(String[] args)
  }

}

// EOF
