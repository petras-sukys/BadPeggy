package com.coderslagoon.badpeggy;

import com.coderslagoon.badpeggy.scanner.ImageFormat;
import com.coderslagoon.baselib.util.Prp;

public class GUIProps {
    private GUIProps() {}

    public static final String GUI_PFX              = "gui.";
    public static final String GUI_DLG_FILEEXTS_PFX = GUI_PFX + "fileexts.";
    public static final String GUI_STATUSBAR_PFX    = GUI_PFX + "statusbar.";
    public static final String GUI_COL_PFX          = GUI_PFX + "col.";
    public static final String GUI_OPTS_PFX         = GUI_PFX + "opts.";
    public static final String GUI_SET_PFX          = GUI_PFX + "set.";

    public static final Prp.Int  SPLITTER_PERCENT    = new Prp.Int (GUI_PFX      + "splpct"        , 50);
    public static final Prp.Int  COL_FILE_WIDTH      = new Prp.Int (GUI_COL_PFX  + "file.width"    , -1);
    public static final Prp.Int  COL_REASON_WIDTH    = new Prp.Int (GUI_COL_PFX  + "reason.width"  , -1);
    public static final Prp.Bool OPTS_LOWPRIO        = new Prp.Bool(GUI_OPTS_PFX + "lowprio"       , true);
    public static final Prp.Bool OPTS_USEALLCPUCORES = new Prp.Bool(GUI_OPTS_PFX + "useallcpucores", true);
    public static final Prp.Bool OPTS_INCSUBFOLDERS  = new Prp.Bool(GUI_OPTS_PFX + "incsubfolders" , true);
    public static final Prp.Str  OPTS_FILEEXTS       = new Prp.Str (GUI_OPTS_PFX + "fileexts"      , makeFileExtensionList());
    public static final Prp.Bool OPTS_IMGVIEWACTIVE  = new Prp.Bool(GUI_OPTS_PFX + "imgviewactive" , true);
    public static final Prp.Bool OPTS_DIFFERENTIATE  = new Prp.Bool(GUI_OPTS_PFX + "differentiate" , true);
    public static final Prp.Str  SET_LASTFOLDER      = new Prp.Str (GUI_SET_PFX  + "lastfolder"    , ".");
    public static final Prp.Str  SET_LASTMOVEDEST    = new Prp.Str (GUI_SET_PFX  + "lastmovedest"  , ".");
    public static final Prp.Str  SET_LASTEXPORTFILE  = new Prp.Str (GUI_SET_PFX  + "lastexportfile", "badpeggy_list.txt");
    public static final Prp.Str  SET_LASTLOGGING     = new Prp.Str (GUI_SET_PFX  + "lastlogging"   , "badpeggy_logging.txt");
    public static final Prp.Str  SET_LANG            = new Prp.Str (GUI_SET_PFX  + "lang"          , null);

    private static String makeFileExtensionList() {
        StringBuilder result = new StringBuilder();
        String comma = "";
        for (ImageFormat ifmt : ImageFormat.values()) {
            for (String ext : ifmt.extensions) {
                result.append(comma + ext);
                comma = ",";
            }
        }
        return result.toString();
    }
}
