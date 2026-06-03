package de.btu.kt.syx.apps.tg55;

import de.btu.kt.syx.test.ASyxTestCase;

public abstract class ATG55TestCase extends ASyxTestCase
{

  // -- Constants -------------------------------------------------------------

  // Yamaha TG55 SysEx Formats
  protected static final String F_TG55_PARCNG1      // Parameter change - s 
    = "43H 1#H 35H 0sH ssH ppH[2] vvH[2]";
  protected static final String F_TG55_PARCNG2      // Parameter change - t,f,e,c
    = "43H 1#H 35H 0tH 0feecccc ppH[2] vvH[2]";
  protected static final String F_TG55_SWITCHREMOTE // Switch Remote
    = "43H 1#H 35H 0DH 00H[2] nnH 00H vvH";
  protected  static final String F_TG55_BULK_REQUEST // Bulk Request
    = "43H 2#H 7AH 4CH 4DH 20H[2] 38H 31H 30H 33H ssH ttH 00H[14] xxH yyH";
  protected  static final String F_TG55_BD_HEADER1   // Bulk header 1
    = "43H 0#H 7AH bbH[2]";
  protected  static final String F_TG55_BD_HEADER2   // Bulk header 2
    = "4CH 4DH 20H[2] 38H 31H 30H 33H ssH ttH 00H[14] xxH yyH";
  protected  static final String F_TG55_BDMU_HEADER  // MU Bulk - Header
    = "aaH bbH ccH ddH eeH ffH ggH hhH iiH jjH 000sssss";
  protected  static final String F_TG55_BDMU_EFFECT  // MU Bulk - Effect
    = "ttH llH ppH qqH rrH";
  protected  static final String F_TG55_BDMU_VOICE   // MU Bulk - Voice
    = "0a000ooo 0000000m 00nnnnnn vvH ttH ssH 00pppppp eeH 0rH";
  
  // Response Timeout
  protected final int TIMEOUT = 1000; // milliseconds

  // -- Getters ---------------------------------------------------------------
  
  protected int getDevNum() 
  {
    return 1;
  }

}

// EOF