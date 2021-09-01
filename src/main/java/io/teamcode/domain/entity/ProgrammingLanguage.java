package io.teamcode.domain.entity;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ProgrammingLanguage {

    C("C", true),
    CSHARP("C#", true),
    CPP("C++", true),
    GO("Go", true),
    HTML_CSS("HTML/CSS", true),
    JAVA("Java", true),
    JAVASCRIPT("JavaScript", true),
    OBJECTIVE_C("Objective-C", true),
    PERL("Perl", true),
    PHP("PHP", true),
    PYTHON("Python", true),
    RUBY("Ruby", true),
    SWIFT("Swift", true),
    ASPDOTNET("ASP.NET", true),

    ABAP("ABAP", false),
    ACTIONSCRIPT("ActionScript", false),
    ADA("Ada", false),
    ARC("Arc", false),
    APEX("Apex", false),
    ASCIIDOC("AsciiDoc", false),
    ANDROID("Android", false),
    ASP("ASP", false),
    ARDUINO("Arduino", false),
    ASSEMBLY("Assembly", false),
    AUTOIT("AutoIt", false),
    BLITZMAX("BlitzMax", false),
    BOO("Boo", false),
    CEYLON("Ceylon", false),
    CLOJURE("Clojure", false),
    COCO("Coco", false),
    COFFEESCRIPT("CoffeeScript", false),
    COLDFUSION("ColdFusion", false),
    COMMON_LISP("Common Lisp", false),
    COMPONENT_PASCAL("Component Pascal", false),
    CSS("CSS", false),
    D("D", false),
    DART("Dart", false),
    DELPHI("Delphi", false),
    DUBY("Duby", false),
    DYLAN("Dylan", false),
    EIFFEL("Eiffel", false),
    ELIXIR("Elixir", false),
    EMACS_LISP("Emacs Lisp", false),
    ERLANG("Erlang", false),
    EUPHORIA("Euphoria", false),
    FSHARP("F#", false),
    FANTOM("Fantom", false),
    FORTH("Forth", false),
    FORTRAN("Fortran", false),
    FOXPRO("FoxPro", false),
    GAMBAS("Gambas", false),
    GROOVY("Groovy", false),
    HACK("Hack", false),
    HASKELL("Haskell", false),
    HAXE("Haxe", false),
    IGOR_PRO("IGOR Pro", false),
    INFORM("Inform", false),
    IO("Io", false),
    JULIA("Julia", false),
    KOTLIN("Kotlin", false),
    LABVIEW("LabVIEW", false),
    LASSO("Lasso", false),
    LATEX("LaTeX", false),
    LIMBO("Limbo", false),
    LIVESCRIPT("LiveScript", false),
    LUA("Lua", false),
    LILYPOND("LilyPond", false),
    M("M", false),
    MARKDOWN("Markdown", false),
    MATHEMATICA("Mathematica", false),
    MATLAB("MATLAB", false),
    MAX_MSP("Max/MSP", false),
    MERCURY("Mercury", false),
    NEMERLE("Nemerle", false),
    NIMROD("Nim", false),
    NODEJS("Node.js", false),
    NU("Nu", false),
    OBJECT_PASCAL("Object Pascal", false),
    OBJECTIVE_J("Objective-J", false),
    OCAML("OCaml", false),
    OCCAM("occam", false),
    OCCAM_PI("occam-π", false),
    OCTAVE("Octave", false),
    OOC("ooc", false),
    OTHER("Other", false),
    OXYGENE("Oxygene", false),
    PL_SQL("PL/SQL", false),
    POWERBASIC("PowerBASIC", false),
    POWERSHELL("PowerShell", false),
    PROCESSING("Processing", false),
    PROLOG("Prolog", false),
    PUPPET("Puppet", false),
    PURE_BASIC("Pure Basic", false),
    PURE_DATA("Pure Data", false),
    QML("QML", false),
    QUORUM("Quorum", false),
    R("R", false),
    RACKET("Racket", false),
    REALBASIC("Realbasic", false),
    RESTRUCTUREDTEXT("reStructuredText", false),
    RUST("Rust", false),
    SASS("Sass/SCSS", false),
    SCALA("Scala", false),
    SCHEME("Scheme", false),
    SCILAB("Scilab", false),
    sclang("sclang", false),
    SELF("Self", false),
    SHELL("Shell", false),
    SMALLTALK("Smalltalk", false),
    SOURCEPAWN("SourcePawn", false),
    SQL("SQL", false),
    STANDARD_ML("Standard ML", false),
    SUPERCOLLIDER("SuperCollider", false),
    TCL("Tcl", false),
    TEX("TeX", false),
    TYPESCRIPT("TypeScript", false),
    UNITYSCRIPT("UnityScript", false),
    UNREALSCRIPT("UnrealScript", false),
    VALA("Vala", false),
    VERILOG("Verilog", false),
    VHDL("VHDL", false),
    VIML("VimL", false),
    VISUAL_BASIC("Visual Basic", false),
    VB_NET("VB.NET", false),
    XML("XML", false),
    XOJO("Xojo", false),
    XPAGES("XPages", false),
    XQUERY("XQuery", false),
    XTEND("XTend", false),
    ZSH("Z Shell", false),
    ;

    private String displayName;

    private boolean popular;

    ProgrammingLanguage(final String displayName, boolean popular) {
        this.displayName = displayName;
        this.popular = popular;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public boolean isPopular() {
        return this.popular;
    }

    public static final List<ProgrammingLanguage> populars() {
        List<ProgrammingLanguage> programmingLanguages = new ArrayList<>();
        for (ProgrammingLanguage pl: ProgrammingLanguage.values()) {
            if (pl.isPopular())
                programmingLanguages.add(pl);
        }

        return programmingLanguages;
    }

    public static final List<ProgrammingLanguage> others() {
        List<ProgrammingLanguage> programmingLanguages = new ArrayList<>();
        for (ProgrammingLanguage pl: ProgrammingLanguage.values()) {
            if (!pl.isPopular())
                programmingLanguages.add(pl);
        }

        return programmingLanguages;
    }
}
