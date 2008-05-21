// $ANTLR 3.0.1 ES3.g3 2008-05-20 16:41:10

package com.google.caja.parser.js.fuzzer;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class ES3Lexer extends Lexer {
    public static final int PACKAGE=52;
    public static final int FUNCTION=17;
    public static final int LOR=95;
    public static final int VT=134;
    public static final int SHR=87;
    public static final int RegularExpressionChar=170;
    public static final int LT=72;
    public static final int WHILE=30;
    public static final int MOD=83;
    public static final int SHL=86;
    public static final int CONST=37;
    public static final int BackslashSequence=168;
    public static final int LS=142;
    public static final int CASE=8;
    public static final int CHAR=35;
    public static final int NEW=21;
    public static final int DO=13;
    public static final int DQUOTE=131;
    public static final int NOT=92;
    public static final int DecimalDigit=152;
    public static final int BYFIELD=114;
    public static final int EOF=-1;
    public static final int BREAK=7;
    public static final int CEXPR=117;
    public static final int DIVASS=110;
    public static final int Identifier=148;
    public static final int BYINDEX=115;
    public static final int INC=84;
    public static final int RPAREN=66;
    public static final int FINAL=43;
    public static final int FORSTEP=120;
    public static final int IMPORT=47;
    public static final int EOL=145;
    public static final int POS=129;
    public static final int OctalDigit=156;
    public static final int RETURN=22;
    public static final int THIS=24;
    public static final int DOUBLE=39;
    public static final int ARGS=111;
    public static final int ExponentPart=157;
    public static final int WhiteSpace=139;
    public static final int VAR=28;
    public static final int EXPORT=41;
    public static final int VOID=29;
    public static final int LABELLED=122;
    public static final int SUPER=58;
    public static final int GOTO=45;
    public static final int EQ=76;
    public static final int XORASS=108;
    public static final int ADDASS=99;
    public static final int ARRAY=112;
    public static final int SHU=88;
    public static final int RBRACK=68;
    public static final int RBRACE=64;
    public static final int PRIVATE=53;
    public static final int STATIC=57;
    public static final int INV=93;
    public static final int SWITCH=23;
    public static final int NULL=4;
    public static final int ELSE=14;
    public static final int NATIVE=51;
    public static final int THROWS=60;
    public static final int INT=48;
    public static final int DELETE=12;
    public static final int MUL=82;
    public static final int IdentifierStartASCII=151;
    public static final int TRY=26;
    public static final int FF=135;
    public static final int SHLASS=103;
    public static final int OctalEscapeSequence=164;
    public static final int USP=138;
    public static final int RegularExpressionFirstChar=169;
    public static final int ANDASS=106;
    public static final int TYPEOF=27;
    public static final int IdentifierNameASCIIStart=154;
    public static final int QUE=96;
    public static final int OR=90;
    public static final int DEBUGGER=38;
    public static final int GT=73;
    public static final int PDEC=127;
    public static final int CALL=116;
    public static final int CharacterEscapeSequence=162;
    public static final int CATCH=9;
    public static final int FALSE=6;
    public static final int EscapeSequence=167;
    public static final int LAND=94;
    public static final int MULASS=101;
    public static final int THROW=25;
    public static final int PINC=128;
    public static final int DEC=85;
    public static final int PROTECTED=54;
    public static final int OctalIntegerLiteral=160;
    public static final int CLASS=36;
    public static final int LBRACK=67;
    public static final int ORASS=107;
    public static final int HexEscapeSequence=165;
    public static final int NAMEDVALUE=123;
    public static final int SingleLineComment=147;
    public static final int GTE=75;
    public static final int LBRACE=63;
    public static final int FOR=16;
    public static final int SUB=81;
    public static final int RegularExpressionLiteral=155;
    public static final int FLOAT=44;
    public static final int ABSTRACT=32;
    public static final int AND=89;
    public static final int DecimalIntegerLiteral=158;
    public static final int LTE=74;
    public static final int HexDigit=150;
    public static final int LPAREN=65;
    public static final int IF=18;
    public static final int SUBASS=100;
    public static final int SYNCHRONIZED=59;
    public static final int BOOLEAN=33;
    public static final int EXPR=118;
    public static final int IN=19;
    public static final int IMPLEMENTS=46;
    public static final int CONTINUE=10;
    public static final int OBJECT=125;
    public static final int COMMA=71;
    public static final int TRANSIENT=61;
    public static final int FORITER=119;
    public static final int MODASS=102;
    public static final int SHRASS=104;
    public static final int PS=143;
    public static final int DOT=69;
    public static final int MultiLineComment=146;
    public static final int IdentifierPart=153;
    public static final int WITH=31;
    public static final int ADD=80;
    public static final int BYTE=34;
    public static final int XOR=91;
    public static final int ZeroToThree=163;
    public static final int VOLATILE=62;
    public static final int ITEM=121;
    public static final int UnicodeEscapeSequence=166;
    public static final int NSAME=79;
    public static final int DEFAULT=11;
    public static final int SHUASS=105;
    public static final int TAB=133;
    public static final int SHORT=56;
    public static final int INSTANCEOF=20;
    public static final int SQUOTE=132;
    public static final int Tokens=171;
    public static final int DecimalLiteral=159;
    public static final int TRUE=5;
    public static final int SAME=78;
    public static final int COLON=97;
    public static final int StringLiteral=149;
    public static final int NEQ=77;
    public static final int PAREXPR=126;
    public static final int ENUM=40;
    public static final int FINALLY=15;
    public static final int NBSP=137;
    public static final int HexIntegerLiteral=161;
    public static final int SP=136;
    public static final int BLOCK=113;
    public static final int NEG=124;
    public static final int LineTerminator=144;
    public static final int ASSIGN=98;
    public static final int INTERFACE=49;
    public static final int DIV=109;
    public static final int SEMIC=70;
    public static final int LONG=50;
    public static final int CR=141;
    public static final int PUBLIC=55;
    public static final int EXTENDS=42;
    public static final int BSLASH=130;
    public static final int LF=140;
    
    private Token last;
    
    private final boolean areRegularExpressionsEnabled()
    {
    	if (last == null)
    	{
    		return true;
    	}
    	switch (last.getType())
    	{
    	// identifier
    		case Identifier:
    	// literals
    		case NULL:
    		case TRUE:
    		case FALSE:
    		case THIS:
    		case OctalIntegerLiteral:
    		case DecimalLiteral:
    		case HexIntegerLiteral:
    		case StringLiteral:
    	// member access ending 
    		case RBRACK:
    	// function call or nested expression ending
    		case RPAREN:
    			return false;
    	// otherwise OK
    		default:
    			return true;
    	}
    }
    	
    private final void consumeIdentifierUnicodeStart() throws RecognitionException, NoViableAltException
    {
    	int ch = input.LA(1);
    	if (isIdentifierStartUnicode(ch))
    	{
    		matchAny();
    		do
    		{
    			ch = input.LA(1);
    			if (ch == '$' || (ch >= '0' && ch <= '9') || (ch >= 'A' && ch <= 'Z') || ch == '\\' || ch == '_' || (ch >= 'a' && ch <= 'z') || isIdentifierPartUnicode(ch))
    			{
    				mIdentifierPart();
    			}
    			else
    			{
    				return;
    			}
    		}
    		while (true);
    	}
    	else
    	{
    		throw new NoViableAltException();
    	}
    }
    	
    private final boolean isIdentifierPartUnicode(int ch)
    {
    	return Character.isJavaIdentifierPart(ch);
    }
    	
    private final boolean isIdentifierStartUnicode(int ch)
    {
    	return Character.isJavaIdentifierStart(ch);
    }
    
    public Token nextToken()
    {
    	Token result = super.nextToken();
    	if (result.getChannel() == Token.DEFAULT_CHANNEL)
    	{
    		last = result;
    	}
    	return result;		
    }

    public ES3Lexer() {;} 
    public ES3Lexer(CharStream input) {
        super(input);
    }
    public String getGrammarFileName() { return "ES3.g3"; }

    // $ANTLR start NULL
    public final void mNULL() throws RecognitionException {
        try {
            int _type = NULL;
            // ES3.g3:89:6: ( 'null' )
            // ES3.g3:89:8: 'null'
            {
            match("null"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end NULL

    // $ANTLR start TRUE
    public final void mTRUE() throws RecognitionException {
        try {
            int _type = TRUE;
            // ES3.g3:90:6: ( 'true' )
            // ES3.g3:90:8: 'true'
            {
            match("true"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end TRUE

    // $ANTLR start FALSE
    public final void mFALSE() throws RecognitionException {
        try {
            int _type = FALSE;
            // ES3.g3:91:7: ( 'false' )
            // ES3.g3:91:9: 'false'
            {
            match("false"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end FALSE

    // $ANTLR start BREAK
    public final void mBREAK() throws RecognitionException {
        try {
            int _type = BREAK;
            // ES3.g3:92:7: ( 'break' )
            // ES3.g3:92:9: 'break'
            {
            match("break"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end BREAK

    // $ANTLR start CASE
    public final void mCASE() throws RecognitionException {
        try {
            int _type = CASE;
            // ES3.g3:93:6: ( 'case' )
            // ES3.g3:93:8: 'case'
            {
            match("case"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end CASE

    // $ANTLR start CATCH
    public final void mCATCH() throws RecognitionException {
        try {
            int _type = CATCH;
            // ES3.g3:94:7: ( 'catch' )
            // ES3.g3:94:9: 'catch'
            {
            match("catch"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end CATCH

    // $ANTLR start CONTINUE
    public final void mCONTINUE() throws RecognitionException {
        try {
            int _type = CONTINUE;
            // ES3.g3:95:10: ( 'continue' )
            // ES3.g3:95:12: 'continue'
            {
            match("continue"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end CONTINUE

    // $ANTLR start DEFAULT
    public final void mDEFAULT() throws RecognitionException {
        try {
            int _type = DEFAULT;
            // ES3.g3:96:9: ( 'default' )
            // ES3.g3:96:11: 'default'
            {
            match("default"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end DEFAULT

    // $ANTLR start DELETE
    public final void mDELETE() throws RecognitionException {
        try {
            int _type = DELETE;
            // ES3.g3:97:8: ( 'delete' )
            // ES3.g3:97:10: 'delete'
            {
            match("delete"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end DELETE

    // $ANTLR start DO
    public final void mDO() throws RecognitionException {
        try {
            int _type = DO;
            // ES3.g3:98:4: ( 'do' )
            // ES3.g3:98:6: 'do'
            {
            match("do"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end DO

    // $ANTLR start ELSE
    public final void mELSE() throws RecognitionException {
        try {
            int _type = ELSE;
            // ES3.g3:99:6: ( 'else' )
            // ES3.g3:99:8: 'else'
            {
            match("else"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end ELSE

    // $ANTLR start FINALLY
    public final void mFINALLY() throws RecognitionException {
        try {
            int _type = FINALLY;
            // ES3.g3:100:9: ( 'finally' )
            // ES3.g3:100:11: 'finally'
            {
            match("finally"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end FINALLY

    // $ANTLR start FOR
    public final void mFOR() throws RecognitionException {
        try {
            int _type = FOR;
            // ES3.g3:101:5: ( 'for' )
            // ES3.g3:101:7: 'for'
            {
            match("for"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end FOR

    // $ANTLR start FUNCTION
    public final void mFUNCTION() throws RecognitionException {
        try {
            int _type = FUNCTION;
            // ES3.g3:102:10: ( 'function' )
            // ES3.g3:102:12: 'function'
            {
            match("function"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end FUNCTION

    // $ANTLR start IF
    public final void mIF() throws RecognitionException {
        try {
            int _type = IF;
            // ES3.g3:103:4: ( 'if' )
            // ES3.g3:103:6: 'if'
            {
            match("if"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end IF

    // $ANTLR start IN
    public final void mIN() throws RecognitionException {
        try {
            int _type = IN;
            // ES3.g3:104:4: ( 'in' )
            // ES3.g3:104:6: 'in'
            {
            match("in"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end IN

    // $ANTLR start INSTANCEOF
    public final void mINSTANCEOF() throws RecognitionException {
        try {
            int _type = INSTANCEOF;
            // ES3.g3:105:12: ( 'instanceof' )
            // ES3.g3:105:14: 'instanceof'
            {
            match("instanceof"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end INSTANCEOF

    // $ANTLR start NEW
    public final void mNEW() throws RecognitionException {
        try {
            int _type = NEW;
            // ES3.g3:106:5: ( 'new' )
            // ES3.g3:106:7: 'new'
            {
            match("new"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end NEW

    // $ANTLR start RETURN
    public final void mRETURN() throws RecognitionException {
        try {
            int _type = RETURN;
            // ES3.g3:107:8: ( 'return' )
            // ES3.g3:107:10: 'return'
            {
            match("return"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end RETURN

    // $ANTLR start SWITCH
    public final void mSWITCH() throws RecognitionException {
        try {
            int _type = SWITCH;
            // ES3.g3:108:8: ( 'switch' )
            // ES3.g3:108:10: 'switch'
            {
            match("switch"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end SWITCH

    // $ANTLR start THIS
    public final void mTHIS() throws RecognitionException {
        try {
            int _type = THIS;
            // ES3.g3:109:6: ( 'this' )
            // ES3.g3:109:8: 'this'
            {
            match("this"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end THIS

    // $ANTLR start THROW
    public final void mTHROW() throws RecognitionException {
        try {
            int _type = THROW;
            // ES3.g3:110:7: ( 'throw' )
            // ES3.g3:110:9: 'throw'
            {
            match("throw"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end THROW

    // $ANTLR start TRY
    public final void mTRY() throws RecognitionException {
        try {
            int _type = TRY;
            // ES3.g3:111:5: ( 'try' )
            // ES3.g3:111:7: 'try'
            {
            match("try"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end TRY

    // $ANTLR start TYPEOF
    public final void mTYPEOF() throws RecognitionException {
        try {
            int _type = TYPEOF;
            // ES3.g3:112:8: ( 'typeof' )
            // ES3.g3:112:10: 'typeof'
            {
            match("typeof"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end TYPEOF

    // $ANTLR start VAR
    public final void mVAR() throws RecognitionException {
        try {
            int _type = VAR;
            // ES3.g3:113:5: ( 'var' )
            // ES3.g3:113:7: 'var'
            {
            match("var"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end VAR

    // $ANTLR start VOID
    public final void mVOID() throws RecognitionException {
        try {
            int _type = VOID;
            // ES3.g3:114:6: ( 'void' )
            // ES3.g3:114:8: 'void'
            {
            match("void"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end VOID

    // $ANTLR start WHILE
    public final void mWHILE() throws RecognitionException {
        try {
            int _type = WHILE;
            // ES3.g3:115:7: ( 'while' )
            // ES3.g3:115:9: 'while'
            {
            match("while"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end WHILE

    // $ANTLR start WITH
    public final void mWITH() throws RecognitionException {
        try {
            int _type = WITH;
            // ES3.g3:116:6: ( 'with' )
            // ES3.g3:116:8: 'with'
            {
            match("with"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end WITH

    // $ANTLR start ABSTRACT
    public final void mABSTRACT() throws RecognitionException {
        try {
            int _type = ABSTRACT;
            // ES3.g3:117:10: ( 'abstract' )
            // ES3.g3:117:12: 'abstract'
            {
            match("abstract"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end ABSTRACT

    // $ANTLR start BOOLEAN
    public final void mBOOLEAN() throws RecognitionException {
        try {
            int _type = BOOLEAN;
            // ES3.g3:118:9: ( 'boolean' )
            // ES3.g3:118:11: 'boolean'
            {
            match("boolean"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end BOOLEAN

    // $ANTLR start BYTE
    public final void mBYTE() throws RecognitionException {
        try {
            int _type = BYTE;
            // ES3.g3:119:6: ( 'byte' )
            // ES3.g3:119:8: 'byte'
            {
            match("byte"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end BYTE

    // $ANTLR start CHAR
    public final void mCHAR() throws RecognitionException {
        try {
            int _type = CHAR;
            // ES3.g3:120:6: ( 'char' )
            // ES3.g3:120:8: 'char'
            {
            match("char"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end CHAR

    // $ANTLR start CLASS
    public final void mCLASS() throws RecognitionException {
        try {
            int _type = CLASS;
            // ES3.g3:121:7: ( 'class' )
            // ES3.g3:121:9: 'class'
            {
            match("class"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end CLASS

    // $ANTLR start CONST
    public final void mCONST() throws RecognitionException {
        try {
            int _type = CONST;
            // ES3.g3:122:7: ( 'const' )
            // ES3.g3:122:9: 'const'
            {
            match("const"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end CONST

    // $ANTLR start DEBUGGER
    public final void mDEBUGGER() throws RecognitionException {
        try {
            int _type = DEBUGGER;
            // ES3.g3:123:10: ( 'debugger' )
            // ES3.g3:123:12: 'debugger'
            {
            match("debugger"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end DEBUGGER

    // $ANTLR start DOUBLE
    public final void mDOUBLE() throws RecognitionException {
        try {
            int _type = DOUBLE;
            // ES3.g3:124:8: ( 'double' )
            // ES3.g3:124:10: 'double'
            {
            match("double"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end DOUBLE

    // $ANTLR start ENUM
    public final void mENUM() throws RecognitionException {
        try {
            int _type = ENUM;
            // ES3.g3:125:6: ( 'enum' )
            // ES3.g3:125:8: 'enum'
            {
            match("enum"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end ENUM

    // $ANTLR start EXPORT
    public final void mEXPORT() throws RecognitionException {
        try {
            int _type = EXPORT;
            // ES3.g3:126:8: ( 'export' )
            // ES3.g3:126:10: 'export'
            {
            match("export"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end EXPORT

    // $ANTLR start EXTENDS
    public final void mEXTENDS() throws RecognitionException {
        try {
            int _type = EXTENDS;
            // ES3.g3:127:9: ( 'extends' )
            // ES3.g3:127:11: 'extends'
            {
            match("extends"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end EXTENDS

    // $ANTLR start FINAL
    public final void mFINAL() throws RecognitionException {
        try {
            int _type = FINAL;
            // ES3.g3:128:7: ( 'final' )
            // ES3.g3:128:9: 'final'
            {
            match("final"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end FINAL

    // $ANTLR start FLOAT
    public final void mFLOAT() throws RecognitionException {
        try {
            int _type = FLOAT;
            // ES3.g3:129:7: ( 'float' )
            // ES3.g3:129:9: 'float'
            {
            match("float"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end FLOAT

    // $ANTLR start GOTO
    public final void mGOTO() throws RecognitionException {
        try {
            int _type = GOTO;
            // ES3.g3:130:6: ( 'goto' )
            // ES3.g3:130:8: 'goto'
            {
            match("goto"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end GOTO

    // $ANTLR start IMPLEMENTS
    public final void mIMPLEMENTS() throws RecognitionException {
        try {
            int _type = IMPLEMENTS;
            // ES3.g3:131:12: ( 'implements' )
            // ES3.g3:131:14: 'implements'
            {
            match("implements"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end IMPLEMENTS

    // $ANTLR start IMPORT
    public final void mIMPORT() throws RecognitionException {
        try {
            int _type = IMPORT;
            // ES3.g3:132:8: ( 'import' )
            // ES3.g3:132:10: 'import'
            {
            match("import"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end IMPORT

    // $ANTLR start INT
    public final void mINT() throws RecognitionException {
        try {
            int _type = INT;
            // ES3.g3:133:5: ( 'int' )
            // ES3.g3:133:7: 'int'
            {
            match("int"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end INT

    // $ANTLR start INTERFACE
    public final void mINTERFACE() throws RecognitionException {
        try {
            int _type = INTERFACE;
            // ES3.g3:134:11: ( 'interface' )
            // ES3.g3:134:13: 'interface'
            {
            match("interface"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end INTERFACE

    // $ANTLR start LONG
    public final void mLONG() throws RecognitionException {
        try {
            int _type = LONG;
            // ES3.g3:135:6: ( 'long' )
            // ES3.g3:135:8: 'long'
            {
            match("long"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end LONG

    // $ANTLR start NATIVE
    public final void mNATIVE() throws RecognitionException {
        try {
            int _type = NATIVE;
            // ES3.g3:136:8: ( 'native' )
            // ES3.g3:136:10: 'native'
            {
            match("native"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end NATIVE

    // $ANTLR start PACKAGE
    public final void mPACKAGE() throws RecognitionException {
        try {
            int _type = PACKAGE;
            // ES3.g3:137:9: ( 'package' )
            // ES3.g3:137:11: 'package'
            {
            match("package"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end PACKAGE

    // $ANTLR start PRIVATE
    public final void mPRIVATE() throws RecognitionException {
        try {
            int _type = PRIVATE;
            // ES3.g3:138:9: ( 'private' )
            // ES3.g3:138:11: 'private'
            {
            match("private"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end PRIVATE

    // $ANTLR start PROTECTED
    public final void mPROTECTED() throws RecognitionException {
        try {
            int _type = PROTECTED;
            // ES3.g3:139:11: ( 'protected' )
            // ES3.g3:139:13: 'protected'
            {
            match("protected"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end PROTECTED

    // $ANTLR start PUBLIC
    public final void mPUBLIC() throws RecognitionException {
        try {
            int _type = PUBLIC;
            // ES3.g3:140:8: ( 'public' )
            // ES3.g3:140:10: 'public'
            {
            match("public"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end PUBLIC

    // $ANTLR start SHORT
    public final void mSHORT() throws RecognitionException {
        try {
            int _type = SHORT;
            // ES3.g3:141:7: ( 'short' )
            // ES3.g3:141:9: 'short'
            {
            match("short"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end SHORT

    // $ANTLR start STATIC
    public final void mSTATIC() throws RecognitionException {
        try {
            int _type = STATIC;
            // ES3.g3:142:8: ( 'static' )
            // ES3.g3:142:10: 'static'
            {
            match("static"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end STATIC

    // $ANTLR start SUPER
    public final void mSUPER() throws RecognitionException {
        try {
            int _type = SUPER;
            // ES3.g3:143:7: ( 'super' )
            // ES3.g3:143:9: 'super'
            {
            match("super"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end SUPER

    // $ANTLR start SYNCHRONIZED
    public final void mSYNCHRONIZED() throws RecognitionException {
        try {
            int _type = SYNCHRONIZED;
            // ES3.g3:144:14: ( 'synchronized' )
            // ES3.g3:144:16: 'synchronized'
            {
            match("synchronized"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end SYNCHRONIZED

    // $ANTLR start THROWS
    public final void mTHROWS() throws RecognitionException {
        try {
            int _type = THROWS;
            // ES3.g3:145:8: ( 'throws' )
            // ES3.g3:145:10: 'throws'
            {
            match("throws"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end THROWS

    // $ANTLR start TRANSIENT
    public final void mTRANSIENT() throws RecognitionException {
        try {
            int _type = TRANSIENT;
            // ES3.g3:146:11: ( 'transient' )
            // ES3.g3:146:13: 'transient'
            {
            match("transient"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end TRANSIENT

    // $ANTLR start VOLATILE
    public final void mVOLATILE() throws RecognitionException {
        try {
            int _type = VOLATILE;
            // ES3.g3:147:10: ( 'volatile' )
            // ES3.g3:147:12: 'volatile'
            {
            match("volatile"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end VOLATILE

    // $ANTLR start LBRACE
    public final void mLBRACE() throws RecognitionException {
        try {
            int _type = LBRACE;
            // ES3.g3:148:8: ( '{' )
            // ES3.g3:148:10: '{'
            {
            match('{'); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end LBRACE

    // $ANTLR start RBRACE
    public final void mRBRACE() throws RecognitionException {
        try {
            int _type = RBRACE;
            // ES3.g3:149:8: ( '}' )
            // ES3.g3:149:10: '}'
            {
            match('}'); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end RBRACE

    // $ANTLR start LPAREN
    public final void mLPAREN() throws RecognitionException {
        try {
            int _type = LPAREN;
            // ES3.g3:150:8: ( '(' )
            // ES3.g3:150:10: '('
            {
            match('('); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end LPAREN

    // $ANTLR start RPAREN
    public final void mRPAREN() throws RecognitionException {
        try {
            int _type = RPAREN;
            // ES3.g3:151:8: ( ')' )
            // ES3.g3:151:10: ')'
            {
            match(')'); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end RPAREN

    // $ANTLR start LBRACK
    public final void mLBRACK() throws RecognitionException {
        try {
            int _type = LBRACK;
            // ES3.g3:152:8: ( '[' )
            // ES3.g3:152:10: '['
            {
            match('['); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end LBRACK

    // $ANTLR start RBRACK
    public final void mRBRACK() throws RecognitionException {
        try {
            int _type = RBRACK;
            // ES3.g3:153:8: ( ']' )
            // ES3.g3:153:10: ']'
            {
            match(']'); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end RBRACK

    // $ANTLR start DOT
    public final void mDOT() throws RecognitionException {
        try {
            int _type = DOT;
            // ES3.g3:154:5: ( '.' )
            // ES3.g3:154:7: '.'
            {
            match('.'); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end DOT

    // $ANTLR start SEMIC
    public final void mSEMIC() throws RecognitionException {
        try {
            int _type = SEMIC;
            // ES3.g3:155:7: ( ';' )
            // ES3.g3:155:9: ';'
            {
            match(';'); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end SEMIC

    // $ANTLR start COMMA
    public final void mCOMMA() throws RecognitionException {
        try {
            int _type = COMMA;
            // ES3.g3:156:7: ( ',' )
            // ES3.g3:156:9: ','
            {
            match(','); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end COMMA

    // $ANTLR start LT
    public final void mLT() throws RecognitionException {
        try {
            int _type = LT;
            // ES3.g3:157:4: ( '<' )
            // ES3.g3:157:6: '<'
            {
            match('<'); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end LT

    // $ANTLR start GT
    public final void mGT() throws RecognitionException {
        try {
            int _type = GT;
            // ES3.g3:158:4: ( '>' )
            // ES3.g3:158:6: '>'
            {
            match('>'); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end GT

    // $ANTLR start LTE
    public final void mLTE() throws RecognitionException {
        try {
            int _type = LTE;
            // ES3.g3:159:5: ( '<=' )
            // ES3.g3:159:7: '<='
            {
            match("<="); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end LTE

    // $ANTLR start GTE
    public final void mGTE() throws RecognitionException {
        try {
            int _type = GTE;
            // ES3.g3:160:5: ( '>=' )
            // ES3.g3:160:7: '>='
            {
            match(">="); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end GTE

    // $ANTLR start EQ
    public final void mEQ() throws RecognitionException {
        try {
            int _type = EQ;
            // ES3.g3:161:4: ( '==' )
            // ES3.g3:161:6: '=='
            {
            match("=="); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end EQ

    // $ANTLR start NEQ
    public final void mNEQ() throws RecognitionException {
        try {
            int _type = NEQ;
            // ES3.g3:162:5: ( '!=' )
            // ES3.g3:162:7: '!='
            {
            match("!="); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end NEQ

    // $ANTLR start SAME
    public final void mSAME() throws RecognitionException {
        try {
            int _type = SAME;
            // ES3.g3:163:6: ( '===' )
            // ES3.g3:163:8: '==='
            {
            match("==="); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end SAME

    // $ANTLR start NSAME
    public final void mNSAME() throws RecognitionException {
        try {
            int _type = NSAME;
            // ES3.g3:164:7: ( '!==' )
            // ES3.g3:164:9: '!=='
            {
            match("!=="); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end NSAME

    // $ANTLR start ADD
    public final void mADD() throws RecognitionException {
        try {
            int _type = ADD;
            // ES3.g3:165:5: ( '+' )
            // ES3.g3:165:7: '+'
            {
            match('+'); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end ADD

    // $ANTLR start SUB
    public final void mSUB() throws RecognitionException {
        try {
            int _type = SUB;
            // ES3.g3:166:5: ( '-' )
            // ES3.g3:166:7: '-'
            {
            match('-'); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end SUB

    // $ANTLR start MUL
    public final void mMUL() throws RecognitionException {
        try {
            int _type = MUL;
            // ES3.g3:167:5: ( '*' )
            // ES3.g3:167:7: '*'
            {
            match('*'); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end MUL

    // $ANTLR start MOD
    public final void mMOD() throws RecognitionException {
        try {
            int _type = MOD;
            // ES3.g3:168:5: ( '%' )
            // ES3.g3:168:7: '%'
            {
            match('%'); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end MOD

    // $ANTLR start INC
    public final void mINC() throws RecognitionException {
        try {
            int _type = INC;
            // ES3.g3:169:5: ( '++' )
            // ES3.g3:169:7: '++'
            {
            match("++"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end INC

    // $ANTLR start DEC
    public final void mDEC() throws RecognitionException {
        try {
            int _type = DEC;
            // ES3.g3:170:5: ( '--' )
            // ES3.g3:170:7: '--'
            {
            match("--"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end DEC

    // $ANTLR start SHL
    public final void mSHL() throws RecognitionException {
        try {
            int _type = SHL;
            // ES3.g3:171:5: ( '<<' )
            // ES3.g3:171:7: '<<'
            {
            match("<<"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end SHL

    // $ANTLR start SHR
    public final void mSHR() throws RecognitionException {
        try {
            int _type = SHR;
            // ES3.g3:172:5: ( '>>' )
            // ES3.g3:172:7: '>>'
            {
            match(">>"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end SHR

    // $ANTLR start SHU
    public final void mSHU() throws RecognitionException {
        try {
            int _type = SHU;
            // ES3.g3:173:5: ( '>>>' )
            // ES3.g3:173:7: '>>>'
            {
            match(">>>"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end SHU

    // $ANTLR start AND
    public final void mAND() throws RecognitionException {
        try {
            int _type = AND;
            // ES3.g3:174:5: ( '&' )
            // ES3.g3:174:7: '&'
            {
            match('&'); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end AND

    // $ANTLR start OR
    public final void mOR() throws RecognitionException {
        try {
            int _type = OR;
            // ES3.g3:175:4: ( '|' )
            // ES3.g3:175:6: '|'
            {
            match('|'); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end OR

    // $ANTLR start XOR
    public final void mXOR() throws RecognitionException {
        try {
            int _type = XOR;
            // ES3.g3:176:5: ( '^' )
            // ES3.g3:176:7: '^'
            {
            match('^'); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end XOR

    // $ANTLR start NOT
    public final void mNOT() throws RecognitionException {
        try {
            int _type = NOT;
            // ES3.g3:177:5: ( '!' )
            // ES3.g3:177:7: '!'
            {
            match('!'); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end NOT

    // $ANTLR start INV
    public final void mINV() throws RecognitionException {
        try {
            int _type = INV;
            // ES3.g3:178:5: ( '~' )
            // ES3.g3:178:7: '~'
            {
            match('~'); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end INV

    // $ANTLR start LAND
    public final void mLAND() throws RecognitionException {
        try {
            int _type = LAND;
            // ES3.g3:179:6: ( '&&' )
            // ES3.g3:179:8: '&&'
            {
            match("&&"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end LAND

    // $ANTLR start LOR
    public final void mLOR() throws RecognitionException {
        try {
            int _type = LOR;
            // ES3.g3:180:5: ( '||' )
            // ES3.g3:180:7: '||'
            {
            match("||"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end LOR

    // $ANTLR start QUE
    public final void mQUE() throws RecognitionException {
        try {
            int _type = QUE;
            // ES3.g3:181:5: ( '?' )
            // ES3.g3:181:7: '?'
            {
            match('?'); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end QUE

    // $ANTLR start COLON
    public final void mCOLON() throws RecognitionException {
        try {
            int _type = COLON;
            // ES3.g3:182:7: ( ':' )
            // ES3.g3:182:9: ':'
            {
            match(':'); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end COLON

    // $ANTLR start ASSIGN
    public final void mASSIGN() throws RecognitionException {
        try {
            int _type = ASSIGN;
            // ES3.g3:183:8: ( '=' )
            // ES3.g3:183:10: '='
            {
            match('='); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end ASSIGN

    // $ANTLR start ADDASS
    public final void mADDASS() throws RecognitionException {
        try {
            int _type = ADDASS;
            // ES3.g3:184:8: ( '+=' )
            // ES3.g3:184:10: '+='
            {
            match("+="); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end ADDASS

    // $ANTLR start SUBASS
    public final void mSUBASS() throws RecognitionException {
        try {
            int _type = SUBASS;
            // ES3.g3:185:8: ( '-=' )
            // ES3.g3:185:10: '-='
            {
            match("-="); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end SUBASS

    // $ANTLR start MULASS
    public final void mMULASS() throws RecognitionException {
        try {
            int _type = MULASS;
            // ES3.g3:186:8: ( '*=' )
            // ES3.g3:186:10: '*='
            {
            match("*="); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end MULASS

    // $ANTLR start MODASS
    public final void mMODASS() throws RecognitionException {
        try {
            int _type = MODASS;
            // ES3.g3:187:8: ( '%=' )
            // ES3.g3:187:10: '%='
            {
            match("%="); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end MODASS

    // $ANTLR start SHLASS
    public final void mSHLASS() throws RecognitionException {
        try {
            int _type = SHLASS;
            // ES3.g3:188:8: ( '<<=' )
            // ES3.g3:188:10: '<<='
            {
            match("<<="); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end SHLASS

    // $ANTLR start SHRASS
    public final void mSHRASS() throws RecognitionException {
        try {
            int _type = SHRASS;
            // ES3.g3:189:8: ( '>>=' )
            // ES3.g3:189:10: '>>='
            {
            match(">>="); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end SHRASS

    // $ANTLR start SHUASS
    public final void mSHUASS() throws RecognitionException {
        try {
            int _type = SHUASS;
            // ES3.g3:190:8: ( '>>>=' )
            // ES3.g3:190:10: '>>>='
            {
            match(">>>="); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end SHUASS

    // $ANTLR start ANDASS
    public final void mANDASS() throws RecognitionException {
        try {
            int _type = ANDASS;
            // ES3.g3:191:8: ( '&=' )
            // ES3.g3:191:10: '&='
            {
            match("&="); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end ANDASS

    // $ANTLR start ORASS
    public final void mORASS() throws RecognitionException {
        try {
            int _type = ORASS;
            // ES3.g3:192:7: ( '|=' )
            // ES3.g3:192:9: '|='
            {
            match("|="); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end ORASS

    // $ANTLR start XORASS
    public final void mXORASS() throws RecognitionException {
        try {
            int _type = XORASS;
            // ES3.g3:193:8: ( '^=' )
            // ES3.g3:193:10: '^='
            {
            match("^="); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end XORASS

    // $ANTLR start DIV
    public final void mDIV() throws RecognitionException {
        try {
            int _type = DIV;
            // ES3.g3:194:5: ( '/' )
            // ES3.g3:194:7: '/'
            {
            match('/'); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end DIV

    // $ANTLR start DIVASS
    public final void mDIVASS() throws RecognitionException {
        try {
            int _type = DIVASS;
            // ES3.g3:195:8: ( '/=' )
            // ES3.g3:195:10: '/='
            {
            match("/="); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end DIVASS

    // $ANTLR start BSLASH
    public final void mBSLASH() throws RecognitionException {
        try {
            // ES3.g3:414:2: ( '\\\\' )
            // ES3.g3:414:4: '\\\\'
            {
            match('\\'); 

            }

        }
        finally {
        }
    }
    // $ANTLR end BSLASH

    // $ANTLR start DQUOTE
    public final void mDQUOTE() throws RecognitionException {
        try {
            // ES3.g3:418:2: ( '\"' )
            // ES3.g3:418:4: '\"'
            {
            match('\"'); 

            }

        }
        finally {
        }
    }
    // $ANTLR end DQUOTE

    // $ANTLR start SQUOTE
    public final void mSQUOTE() throws RecognitionException {
        try {
            // ES3.g3:422:2: ( '\\'' )
            // ES3.g3:422:4: '\\''
            {
            match('\''); 

            }

        }
        finally {
        }
    }
    // $ANTLR end SQUOTE

    // $ANTLR start TAB
    public final void mTAB() throws RecognitionException {
        try {
            // ES3.g3:428:2: ( '\\u0009' )
            // ES3.g3:428:4: '\\u0009'
            {
            match('\t'); 

            }

        }
        finally {
        }
    }
    // $ANTLR end TAB

    // $ANTLR start VT
    public final void mVT() throws RecognitionException {
        try {
            // ES3.g3:432:2: ( '\\u000b' )
            // ES3.g3:432:4: '\\u000b'
            {
            match('\u000B'); 

            }

        }
        finally {
        }
    }
    // $ANTLR end VT

    // $ANTLR start FF
    public final void mFF() throws RecognitionException {
        try {
            // ES3.g3:436:2: ( '\\u000c' )
            // ES3.g3:436:4: '\\u000c'
            {
            match('\f'); 

            }

        }
        finally {
        }
    }
    // $ANTLR end FF

    // $ANTLR start SP
    public final void mSP() throws RecognitionException {
        try {
            // ES3.g3:440:2: ( '\\u0020' )
            // ES3.g3:440:4: '\\u0020'
            {
            match(' '); 

            }

        }
        finally {
        }
    }
    // $ANTLR end SP

    // $ANTLR start NBSP
    public final void mNBSP() throws RecognitionException {
        try {
            // ES3.g3:444:2: ( '\\u00a0' )
            // ES3.g3:444:4: '\\u00a0'
            {
            match('\u00A0'); 

            }

        }
        finally {
        }
    }
    // $ANTLR end NBSP

    // $ANTLR start USP
    public final void mUSP() throws RecognitionException {
        try {
            // ES3.g3:448:2: ( '\\u1680' | '\\u180E' | '\\u2000' | '\\u2001' | '\\u2002' | '\\u2003' | '\\u2004' | '\\u2005' | '\\u2006' | '\\u2007' | '\\u2008' | '\\u2009' | '\\u200A' | '\\u202F' | '\\u205F' | '\\u3000' )
            // ES3.g3:
            {
            if ( input.LA(1)=='\u1680'||input.LA(1)=='\u180E'||(input.LA(1)>='\u2000' && input.LA(1)<='\u200A')||input.LA(1)=='\u202F'||input.LA(1)=='\u205F'||input.LA(1)=='\u3000' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }


            }

        }
        finally {
        }
    }
    // $ANTLR end USP

    // $ANTLR start WhiteSpace
    public final void mWhiteSpace() throws RecognitionException {
        try {
            int _type = WhiteSpace;
            // ES3.g3:467:2: ( ( TAB | VT | FF | SP | NBSP | USP )+ )
            // ES3.g3:467:4: ( TAB | VT | FF | SP | NBSP | USP )+
            {
            // ES3.g3:467:4: ( TAB | VT | FF | SP | NBSP | USP )+
            int cnt1=0;
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( (LA1_0=='\t'||(LA1_0>='\u000B' && LA1_0<='\f')||LA1_0==' '||LA1_0=='\u00A0'||LA1_0=='\u1680'||LA1_0=='\u180E'||(LA1_0>='\u2000' && LA1_0<='\u200A')||LA1_0=='\u202F'||LA1_0=='\u205F'||LA1_0=='\u3000') ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // ES3.g3:
            	    {
            	    if ( input.LA(1)=='\t'||(input.LA(1)>='\u000B' && input.LA(1)<='\f')||input.LA(1)==' '||input.LA(1)=='\u00A0'||input.LA(1)=='\u1680'||input.LA(1)=='\u180E'||(input.LA(1)>='\u2000' && input.LA(1)<='\u200A')||input.LA(1)=='\u202F'||input.LA(1)=='\u205F'||input.LA(1)=='\u3000' ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse =
            	            new MismatchedSetException(null,input);
            	        recover(mse);    throw mse;
            	    }


            	    }
            	    break;

            	default :
            	    if ( cnt1 >= 1 ) break loop1;
                        EarlyExitException eee =
                            new EarlyExitException(1, input);
                        throw eee;
                }
                cnt1++;
            } while (true);

             channel = HIDDEN; 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end WhiteSpace

    // $ANTLR start LF
    public final void mLF() throws RecognitionException {
        try {
            // ES3.g3:475:2: ( '\\n' )
            // ES3.g3:475:4: '\\n'
            {
            match('\n'); 

            }

        }
        finally {
        }
    }
    // $ANTLR end LF

    // $ANTLR start CR
    public final void mCR() throws RecognitionException {
        try {
            // ES3.g3:479:2: ( '\\r' )
            // ES3.g3:479:4: '\\r'
            {
            match('\r'); 

            }

        }
        finally {
        }
    }
    // $ANTLR end CR

    // $ANTLR start LS
    public final void mLS() throws RecognitionException {
        try {
            // ES3.g3:483:2: ( '\\u2028' )
            // ES3.g3:483:4: '\\u2028'
            {
            match('\u2028'); 

            }

        }
        finally {
        }
    }
    // $ANTLR end LS

    // $ANTLR start PS
    public final void mPS() throws RecognitionException {
        try {
            // ES3.g3:487:2: ( '\\u2029' )
            // ES3.g3:487:4: '\\u2029'
            {
            match('\u2029'); 

            }

        }
        finally {
        }
    }
    // $ANTLR end PS

    // $ANTLR start LineTerminator
    public final void mLineTerminator() throws RecognitionException {
        try {
            // ES3.g3:491:2: ( CR | LF | LS | PS )
            // ES3.g3:
            {
            if ( input.LA(1)=='\n'||input.LA(1)=='\r'||(input.LA(1)>='\u2028' && input.LA(1)<='\u2029') ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }


            }

        }
        finally {
        }
    }
    // $ANTLR end LineTerminator

    // $ANTLR start EOL
    public final void mEOL() throws RecognitionException {
        try {
            int _type = EOL;
            // ES3.g3:495:2: ( ( ( CR ( LF )? ) | LF | LS | PS ) )
            // ES3.g3:495:4: ( ( CR ( LF )? ) | LF | LS | PS )
            {
            // ES3.g3:495:4: ( ( CR ( LF )? ) | LF | LS | PS )
            int alt3=4;
            switch ( input.LA(1) ) {
            case '\r':
                {
                alt3=1;
                }
                break;
            case '\n':
                {
                alt3=2;
                }
                break;
            case '\u2028':
                {
                alt3=3;
                }
                break;
            case '\u2029':
                {
                alt3=4;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("495:4: ( ( CR ( LF )? ) | LF | LS | PS )", 3, 0, input);

                throw nvae;
            }

            switch (alt3) {
                case 1 :
                    // ES3.g3:495:6: ( CR ( LF )? )
                    {
                    // ES3.g3:495:6: ( CR ( LF )? )
                    // ES3.g3:495:8: CR ( LF )?
                    {
                    mCR(); 
                    // ES3.g3:495:11: ( LF )?
                    int alt2=2;
                    int LA2_0 = input.LA(1);

                    if ( (LA2_0=='\n') ) {
                        alt2=1;
                    }
                    switch (alt2) {
                        case 1 :
                            // ES3.g3:495:11: LF
                            {
                            mLF(); 

                            }
                            break;

                    }


                    }


                    }
                    break;
                case 2 :
                    // ES3.g3:495:19: LF
                    {
                    mLF(); 

                    }
                    break;
                case 3 :
                    // ES3.g3:495:24: LS
                    {
                    mLS(); 

                    }
                    break;
                case 4 :
                    // ES3.g3:495:29: PS
                    {
                    mPS(); 

                    }
                    break;

            }

             channel = HIDDEN; 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end EOL

    // $ANTLR start MultiLineComment
    public final void mMultiLineComment() throws RecognitionException {
        try {
            int _type = MultiLineComment;
            // ES3.g3:502:2: ( '/*' ( options {greedy=false; } : . )* '*/' )
            // ES3.g3:502:4: '/*' ( options {greedy=false; } : . )* '*/'
            {
            match("/*"); 

            // ES3.g3:502:9: ( options {greedy=false; } : . )*
            loop4:
            do {
                int alt4=2;
                int LA4_0 = input.LA(1);

                if ( (LA4_0=='*') ) {
                    int LA4_1 = input.LA(2);

                    if ( (LA4_1=='/') ) {
                        alt4=2;
                    }
                    else if ( ((LA4_1>='\u0000' && LA4_1<='.')||(LA4_1>='0' && LA4_1<='\uFFFE')) ) {
                        alt4=1;
                    }


                }
                else if ( ((LA4_0>='\u0000' && LA4_0<=')')||(LA4_0>='+' && LA4_0<='\uFFFE')) ) {
                    alt4=1;
                }


                switch (alt4) {
            	case 1 :
            	    // ES3.g3:502:41: .
            	    {
            	    matchAny(); 

            	    }
            	    break;

            	default :
            	    break loop4;
                }
            } while (true);

            match("*/"); 

             channel = HIDDEN; 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end MultiLineComment

    // $ANTLR start SingleLineComment
    public final void mSingleLineComment() throws RecognitionException {
        try {
            int _type = SingleLineComment;
            // ES3.g3:506:2: ( '//' (~ ( LineTerminator ) )* )
            // ES3.g3:506:4: '//' (~ ( LineTerminator ) )*
            {
            match("//"); 

            // ES3.g3:506:9: (~ ( LineTerminator ) )*
            loop5:
            do {
                int alt5=2;
                int LA5_0 = input.LA(1);

                if ( ((LA5_0>='\u0000' && LA5_0<='\t')||(LA5_0>='\u000B' && LA5_0<='\f')||(LA5_0>='\u000E' && LA5_0<='\u2027')||(LA5_0>='\u202A' && LA5_0<='\uFFFE')) ) {
                    alt5=1;
                }


                switch (alt5) {
            	case 1 :
            	    // ES3.g3:506:11: ~ ( LineTerminator )
            	    {
            	    if ( (input.LA(1)>='\u0000' && input.LA(1)<='\t')||(input.LA(1)>='\u000B' && input.LA(1)<='\f')||(input.LA(1)>='\u000E' && input.LA(1)<='\u2027')||(input.LA(1)>='\u202A' && input.LA(1)<='\uFFFE') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse =
            	            new MismatchedSetException(null,input);
            	        recover(mse);    throw mse;
            	    }


            	    }
            	    break;

            	default :
            	    break loop5;
                }
            } while (true);

             channel = HIDDEN; 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end SingleLineComment

    // $ANTLR start IdentifierStartASCII
    public final void mIdentifierStartASCII() throws RecognitionException {
        try {
            // ES3.g3:607:2: ( 'a' .. 'z' | 'A' .. 'Z' | '$' | '_' | BSLASH 'u' HexDigit HexDigit HexDigit HexDigit )
            int alt6=5;
            switch ( input.LA(1) ) {
            case 'a':
            case 'b':
            case 'c':
            case 'd':
            case 'e':
            case 'f':
            case 'g':
            case 'h':
            case 'i':
            case 'j':
            case 'k':
            case 'l':
            case 'm':
            case 'n':
            case 'o':
            case 'p':
            case 'q':
            case 'r':
            case 's':
            case 't':
            case 'u':
            case 'v':
            case 'w':
            case 'x':
            case 'y':
            case 'z':
                {
                alt6=1;
                }
                break;
            case 'A':
            case 'B':
            case 'C':
            case 'D':
            case 'E':
            case 'F':
            case 'G':
            case 'H':
            case 'I':
            case 'J':
            case 'K':
            case 'L':
            case 'M':
            case 'N':
            case 'O':
            case 'P':
            case 'Q':
            case 'R':
            case 'S':
            case 'T':
            case 'U':
            case 'V':
            case 'W':
            case 'X':
            case 'Y':
            case 'Z':
                {
                alt6=2;
                }
                break;
            case '$':
                {
                alt6=3;
                }
                break;
            case '_':
                {
                alt6=4;
                }
                break;
            case '\\':
                {
                alt6=5;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("606:10: fragment IdentifierStartASCII : ( 'a' .. 'z' | 'A' .. 'Z' | '$' | '_' | BSLASH 'u' HexDigit HexDigit HexDigit HexDigit );", 6, 0, input);

                throw nvae;
            }

            switch (alt6) {
                case 1 :
                    // ES3.g3:607:4: 'a' .. 'z'
                    {
                    matchRange('a','z'); 

                    }
                    break;
                case 2 :
                    // ES3.g3:607:15: 'A' .. 'Z'
                    {
                    matchRange('A','Z'); 

                    }
                    break;
                case 3 :
                    // ES3.g3:608:4: '$'
                    {
                    match('$'); 

                    }
                    break;
                case 4 :
                    // ES3.g3:609:4: '_'
                    {
                    match('_'); 

                    }
                    break;
                case 5 :
                    // ES3.g3:610:4: BSLASH 'u' HexDigit HexDigit HexDigit HexDigit
                    {
                    mBSLASH(); 
                    match('u'); 
                    mHexDigit(); 
                    mHexDigit(); 
                    mHexDigit(); 
                    mHexDigit(); 

                    }
                    break;

            }
        }
        finally {
        }
    }
    // $ANTLR end IdentifierStartASCII

    // $ANTLR start IdentifierPart
    public final void mIdentifierPart() throws RecognitionException {
        try {
            // ES3.g3:618:2: ( DecimalDigit | IdentifierStartASCII | {...}?)
            int alt7=3;
            switch ( input.LA(1) ) {
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                {
                alt7=1;
                }
                break;
            case '$':
            case 'A':
            case 'B':
            case 'C':
            case 'D':
            case 'E':
            case 'F':
            case 'G':
            case 'H':
            case 'I':
            case 'J':
            case 'K':
            case 'L':
            case 'M':
            case 'N':
            case 'O':
            case 'P':
            case 'Q':
            case 'R':
            case 'S':
            case 'T':
            case 'U':
            case 'V':
            case 'W':
            case 'X':
            case 'Y':
            case 'Z':
            case '\\':
            case '_':
            case 'a':
            case 'b':
            case 'c':
            case 'd':
            case 'e':
            case 'f':
            case 'g':
            case 'h':
            case 'i':
            case 'j':
            case 'k':
            case 'l':
            case 'm':
            case 'n':
            case 'o':
            case 'p':
            case 'q':
            case 'r':
            case 's':
            case 't':
            case 'u':
            case 'v':
            case 'w':
            case 'x':
            case 'y':
            case 'z':
                {
                alt7=2;
                }
                break;
            default:
                alt7=3;}

            switch (alt7) {
                case 1 :
                    // ES3.g3:618:4: DecimalDigit
                    {
                    mDecimalDigit(); 

                    }
                    break;
                case 2 :
                    // ES3.g3:619:4: IdentifierStartASCII
                    {
                    mIdentifierStartASCII(); 

                    }
                    break;
                case 3 :
                    // ES3.g3:620:4: {...}?
                    {
                    if ( !( isIdentifierPartUnicode(input.LA(1)) ) ) {
                        throw new FailedPredicateException(input, "IdentifierPart", " isIdentifierPartUnicode(input.LA(1)) ");
                    }
                     matchAny(); 

                    }
                    break;

            }
        }
        finally {
        }
    }
    // $ANTLR end IdentifierPart

    // $ANTLR start IdentifierNameASCIIStart
    public final void mIdentifierNameASCIIStart() throws RecognitionException {
        try {
            // ES3.g3:624:2: ( IdentifierStartASCII ( IdentifierPart )* )
            // ES3.g3:624:4: IdentifierStartASCII ( IdentifierPart )*
            {
            mIdentifierStartASCII(); 
            // ES3.g3:624:25: ( IdentifierPart )*
            loop8:
            do {
                int alt8=2;
                int LA8_0 = input.LA(1);

                if ( (LA8_0=='$'||(LA8_0>='0' && LA8_0<='9')||(LA8_0>='A' && LA8_0<='Z')||LA8_0=='\\'||LA8_0=='_'||(LA8_0>='a' && LA8_0<='z')) ) {
                    alt8=1;
                }
                else if ( ( isIdentifierPartUnicode(input.LA(1)) ) ) {
                    alt8=1;
                }


                switch (alt8) {
            	case 1 :
            	    // ES3.g3:624:25: IdentifierPart
            	    {
            	    mIdentifierPart(); 

            	    }
            	    break;

            	default :
            	    break loop8;
                }
            } while (true);


            }

        }
        finally {
        }
    }
    // $ANTLR end IdentifierNameASCIIStart

    // $ANTLR start Identifier
    public final void mIdentifier() throws RecognitionException {
        try {
            int _type = Identifier;
            // ES3.g3:636:2: ( IdentifierNameASCIIStart | )
            int alt9=2;
            int LA9_0 = input.LA(1);

            if ( (LA9_0=='$'||(LA9_0>='A' && LA9_0<='Z')||LA9_0=='\\'||LA9_0=='_'||(LA9_0>='a' && LA9_0<='z')) ) {
                alt9=1;
            }
            else {
                alt9=2;}
            switch (alt9) {
                case 1 :
                    // ES3.g3:636:4: IdentifierNameASCIIStart
                    {
                    mIdentifierNameASCIIStart(); 

                    }
                    break;
                case 2 :
                    // ES3.g3:637:4: 
                    {
                     consumeIdentifierUnicodeStart(); 

                    }
                    break;

            }
            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end Identifier

    // $ANTLR start DecimalDigit
    public final void mDecimalDigit() throws RecognitionException {
        try {
            // ES3.g3:720:2: ( '0' .. '9' )
            // ES3.g3:720:4: '0' .. '9'
            {
            matchRange('0','9'); 

            }

        }
        finally {
        }
    }
    // $ANTLR end DecimalDigit

    // $ANTLR start HexDigit
    public final void mHexDigit() throws RecognitionException {
        try {
            // ES3.g3:724:2: ( DecimalDigit | 'a' .. 'f' | 'A' .. 'F' )
            // ES3.g3:
            {
            if ( (input.LA(1)>='0' && input.LA(1)<='9')||(input.LA(1)>='A' && input.LA(1)<='F')||(input.LA(1)>='a' && input.LA(1)<='f') ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }


            }

        }
        finally {
        }
    }
    // $ANTLR end HexDigit

    // $ANTLR start OctalDigit
    public final void mOctalDigit() throws RecognitionException {
        try {
            // ES3.g3:728:2: ( '0' .. '7' )
            // ES3.g3:728:4: '0' .. '7'
            {
            matchRange('0','7'); 

            }

        }
        finally {
        }
    }
    // $ANTLR end OctalDigit

    // $ANTLR start ExponentPart
    public final void mExponentPart() throws RecognitionException {
        try {
            // ES3.g3:732:2: ( ( 'e' | 'E' ) ( '+' | '-' )? ( DecimalDigit )+ )
            // ES3.g3:732:4: ( 'e' | 'E' ) ( '+' | '-' )? ( DecimalDigit )+
            {
            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            // ES3.g3:732:18: ( '+' | '-' )?
            int alt10=2;
            int LA10_0 = input.LA(1);

            if ( (LA10_0=='+'||LA10_0=='-') ) {
                alt10=1;
            }
            switch (alt10) {
                case 1 :
                    // ES3.g3:
                    {
                    if ( input.LA(1)=='+'||input.LA(1)=='-' ) {
                        input.consume();

                    }
                    else {
                        MismatchedSetException mse =
                            new MismatchedSetException(null,input);
                        recover(mse);    throw mse;
                    }


                    }
                    break;

            }

            // ES3.g3:732:33: ( DecimalDigit )+
            int cnt11=0;
            loop11:
            do {
                int alt11=2;
                int LA11_0 = input.LA(1);

                if ( ((LA11_0>='0' && LA11_0<='9')) ) {
                    alt11=1;
                }


                switch (alt11) {
            	case 1 :
            	    // ES3.g3:732:33: DecimalDigit
            	    {
            	    mDecimalDigit(); 

            	    }
            	    break;

            	default :
            	    if ( cnt11 >= 1 ) break loop11;
                        EarlyExitException eee =
                            new EarlyExitException(11, input);
                        throw eee;
                }
                cnt11++;
            } while (true);


            }

        }
        finally {
        }
    }
    // $ANTLR end ExponentPart

    // $ANTLR start DecimalIntegerLiteral
    public final void mDecimalIntegerLiteral() throws RecognitionException {
        try {
            // ES3.g3:736:2: ( '0' | '1' .. '9' ( DecimalDigit )* )
            int alt13=2;
            int LA13_0 = input.LA(1);

            if ( (LA13_0=='0') ) {
                alt13=1;
            }
            else if ( ((LA13_0>='1' && LA13_0<='9')) ) {
                alt13=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("735:10: fragment DecimalIntegerLiteral : ( '0' | '1' .. '9' ( DecimalDigit )* );", 13, 0, input);

                throw nvae;
            }
            switch (alt13) {
                case 1 :
                    // ES3.g3:736:4: '0'
                    {
                    match('0'); 

                    }
                    break;
                case 2 :
                    // ES3.g3:737:4: '1' .. '9' ( DecimalDigit )*
                    {
                    matchRange('1','9'); 
                    // ES3.g3:737:13: ( DecimalDigit )*
                    loop12:
                    do {
                        int alt12=2;
                        int LA12_0 = input.LA(1);

                        if ( ((LA12_0>='0' && LA12_0<='9')) ) {
                            alt12=1;
                        }


                        switch (alt12) {
                    	case 1 :
                    	    // ES3.g3:737:13: DecimalDigit
                    	    {
                    	    mDecimalDigit(); 

                    	    }
                    	    break;

                    	default :
                    	    break loop12;
                        }
                    } while (true);


                    }
                    break;

            }
        }
        finally {
        }
    }
    // $ANTLR end DecimalIntegerLiteral

    // $ANTLR start DecimalLiteral
    public final void mDecimalLiteral() throws RecognitionException {
        try {
            int _type = DecimalLiteral;
            // ES3.g3:741:2: ( DecimalIntegerLiteral '.' ( DecimalDigit )* ( ExponentPart )? | '.' ( DecimalDigit )+ ( ExponentPart )? | DecimalIntegerLiteral ( ExponentPart )? )
            int alt19=3;
            alt19 = dfa19.predict(input);
            switch (alt19) {
                case 1 :
                    // ES3.g3:741:4: DecimalIntegerLiteral '.' ( DecimalDigit )* ( ExponentPart )?
                    {
                    mDecimalIntegerLiteral(); 
                    match('.'); 
                    // ES3.g3:741:30: ( DecimalDigit )*
                    loop14:
                    do {
                        int alt14=2;
                        int LA14_0 = input.LA(1);

                        if ( ((LA14_0>='0' && LA14_0<='9')) ) {
                            alt14=1;
                        }


                        switch (alt14) {
                    	case 1 :
                    	    // ES3.g3:741:30: DecimalDigit
                    	    {
                    	    mDecimalDigit(); 

                    	    }
                    	    break;

                    	default :
                    	    break loop14;
                        }
                    } while (true);

                    // ES3.g3:741:44: ( ExponentPart )?
                    int alt15=2;
                    int LA15_0 = input.LA(1);

                    if ( (LA15_0=='E'||LA15_0=='e') ) {
                        alt15=1;
                    }
                    switch (alt15) {
                        case 1 :
                            // ES3.g3:741:44: ExponentPart
                            {
                            mExponentPart(); 

                            }
                            break;

                    }


                    }
                    break;
                case 2 :
                    // ES3.g3:742:4: '.' ( DecimalDigit )+ ( ExponentPart )?
                    {
                    match('.'); 
                    // ES3.g3:742:8: ( DecimalDigit )+
                    int cnt16=0;
                    loop16:
                    do {
                        int alt16=2;
                        int LA16_0 = input.LA(1);

                        if ( ((LA16_0>='0' && LA16_0<='9')) ) {
                            alt16=1;
                        }


                        switch (alt16) {
                    	case 1 :
                    	    // ES3.g3:742:8: DecimalDigit
                    	    {
                    	    mDecimalDigit(); 

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt16 >= 1 ) break loop16;
                                EarlyExitException eee =
                                    new EarlyExitException(16, input);
                                throw eee;
                        }
                        cnt16++;
                    } while (true);

                    // ES3.g3:742:22: ( ExponentPart )?
                    int alt17=2;
                    int LA17_0 = input.LA(1);

                    if ( (LA17_0=='E'||LA17_0=='e') ) {
                        alt17=1;
                    }
                    switch (alt17) {
                        case 1 :
                            // ES3.g3:742:22: ExponentPart
                            {
                            mExponentPart(); 

                            }
                            break;

                    }


                    }
                    break;
                case 3 :
                    // ES3.g3:743:4: DecimalIntegerLiteral ( ExponentPart )?
                    {
                    mDecimalIntegerLiteral(); 
                    // ES3.g3:743:26: ( ExponentPart )?
                    int alt18=2;
                    int LA18_0 = input.LA(1);

                    if ( (LA18_0=='E'||LA18_0=='e') ) {
                        alt18=1;
                    }
                    switch (alt18) {
                        case 1 :
                            // ES3.g3:743:26: ExponentPart
                            {
                            mExponentPart(); 

                            }
                            break;

                    }


                    }
                    break;

            }
            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end DecimalLiteral

    // $ANTLR start OctalIntegerLiteral
    public final void mOctalIntegerLiteral() throws RecognitionException {
        try {
            int _type = OctalIntegerLiteral;
            // ES3.g3:747:2: ( '0' ( OctalDigit )+ )
            // ES3.g3:747:4: '0' ( OctalDigit )+
            {
            match('0'); 
            // ES3.g3:747:8: ( OctalDigit )+
            int cnt20=0;
            loop20:
            do {
                int alt20=2;
                int LA20_0 = input.LA(1);

                if ( ((LA20_0>='0' && LA20_0<='7')) ) {
                    alt20=1;
                }


                switch (alt20) {
            	case 1 :
            	    // ES3.g3:747:8: OctalDigit
            	    {
            	    mOctalDigit(); 

            	    }
            	    break;

            	default :
            	    if ( cnt20 >= 1 ) break loop20;
                        EarlyExitException eee =
                            new EarlyExitException(20, input);
                        throw eee;
                }
                cnt20++;
            } while (true);


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end OctalIntegerLiteral

    // $ANTLR start HexIntegerLiteral
    public final void mHexIntegerLiteral() throws RecognitionException {
        try {
            int _type = HexIntegerLiteral;
            // ES3.g3:751:2: ( ( '0x' | '0X' ) ( HexDigit )+ )
            // ES3.g3:751:4: ( '0x' | '0X' ) ( HexDigit )+
            {
            // ES3.g3:751:4: ( '0x' | '0X' )
            int alt21=2;
            int LA21_0 = input.LA(1);

            if ( (LA21_0=='0') ) {
                int LA21_1 = input.LA(2);

                if ( (LA21_1=='X') ) {
                    alt21=2;
                }
                else if ( (LA21_1=='x') ) {
                    alt21=1;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("751:4: ( '0x' | '0X' )", 21, 1, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("751:4: ( '0x' | '0X' )", 21, 0, input);

                throw nvae;
            }
            switch (alt21) {
                case 1 :
                    // ES3.g3:751:6: '0x'
                    {
                    match("0x"); 


                    }
                    break;
                case 2 :
                    // ES3.g3:751:13: '0X'
                    {
                    match("0X"); 


                    }
                    break;

            }

            // ES3.g3:751:20: ( HexDigit )+
            int cnt22=0;
            loop22:
            do {
                int alt22=2;
                int LA22_0 = input.LA(1);

                if ( ((LA22_0>='0' && LA22_0<='9')||(LA22_0>='A' && LA22_0<='F')||(LA22_0>='a' && LA22_0<='f')) ) {
                    alt22=1;
                }


                switch (alt22) {
            	case 1 :
            	    // ES3.g3:751:20: HexDigit
            	    {
            	    mHexDigit(); 

            	    }
            	    break;

            	default :
            	    if ( cnt22 >= 1 ) break loop22;
                        EarlyExitException eee =
                            new EarlyExitException(22, input);
                        throw eee;
                }
                cnt22++;
            } while (true);


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end HexIntegerLiteral

    // $ANTLR start CharacterEscapeSequence
    public final void mCharacterEscapeSequence() throws RecognitionException {
        try {
            // ES3.g3:770:2: (~ ( DecimalDigit | 'x' | 'u' | LineTerminator ) )
            // ES3.g3:770:4: ~ ( DecimalDigit | 'x' | 'u' | LineTerminator )
            {
            if ( (input.LA(1)>='\u0000' && input.LA(1)<='\t')||(input.LA(1)>='\u000B' && input.LA(1)<='\f')||(input.LA(1)>='\u000E' && input.LA(1)<='/')||(input.LA(1)>=':' && input.LA(1)<='t')||(input.LA(1)>='v' && input.LA(1)<='w')||(input.LA(1)>='y' && input.LA(1)<='\u2027')||(input.LA(1)>='\u202A' && input.LA(1)<='\uFFFE') ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }


            }

        }
        finally {
        }
    }
    // $ANTLR end CharacterEscapeSequence

    // $ANTLR start ZeroToThree
    public final void mZeroToThree() throws RecognitionException {
        try {
            // ES3.g3:774:2: ( '0' .. '3' )
            // ES3.g3:774:4: '0' .. '3'
            {
            matchRange('0','3'); 

            }

        }
        finally {
        }
    }
    // $ANTLR end ZeroToThree

    // $ANTLR start OctalEscapeSequence
    public final void mOctalEscapeSequence() throws RecognitionException {
        try {
            // ES3.g3:778:2: ( OctalDigit | ZeroToThree OctalDigit | '4' .. '7' OctalDigit | ZeroToThree OctalDigit OctalDigit )
            int alt23=4;
            int LA23_0 = input.LA(1);

            if ( ((LA23_0>='0' && LA23_0<='3')) ) {
                int LA23_1 = input.LA(2);

                if ( ((LA23_1>='0' && LA23_1<='7')) ) {
                    int LA23_4 = input.LA(3);

                    if ( ((LA23_4>='0' && LA23_4<='7')) ) {
                        alt23=4;
                    }
                    else {
                        alt23=2;}
                }
                else {
                    alt23=1;}
            }
            else if ( ((LA23_0>='4' && LA23_0<='7')) ) {
                int LA23_2 = input.LA(2);

                if ( ((LA23_2>='0' && LA23_2<='7')) ) {
                    alt23=3;
                }
                else {
                    alt23=1;}
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("777:10: fragment OctalEscapeSequence : ( OctalDigit | ZeroToThree OctalDigit | '4' .. '7' OctalDigit | ZeroToThree OctalDigit OctalDigit );", 23, 0, input);

                throw nvae;
            }
            switch (alt23) {
                case 1 :
                    // ES3.g3:778:4: OctalDigit
                    {
                    mOctalDigit(); 

                    }
                    break;
                case 2 :
                    // ES3.g3:779:4: ZeroToThree OctalDigit
                    {
                    mZeroToThree(); 
                    mOctalDigit(); 

                    }
                    break;
                case 3 :
                    // ES3.g3:780:4: '4' .. '7' OctalDigit
                    {
                    matchRange('4','7'); 
                    mOctalDigit(); 

                    }
                    break;
                case 4 :
                    // ES3.g3:781:4: ZeroToThree OctalDigit OctalDigit
                    {
                    mZeroToThree(); 
                    mOctalDigit(); 
                    mOctalDigit(); 

                    }
                    break;

            }
        }
        finally {
        }
    }
    // $ANTLR end OctalEscapeSequence

    // $ANTLR start HexEscapeSequence
    public final void mHexEscapeSequence() throws RecognitionException {
        try {
            // ES3.g3:785:2: ( 'x' HexDigit HexDigit )
            // ES3.g3:785:4: 'x' HexDigit HexDigit
            {
            match('x'); 
            mHexDigit(); 
            mHexDigit(); 

            }

        }
        finally {
        }
    }
    // $ANTLR end HexEscapeSequence

    // $ANTLR start UnicodeEscapeSequence
    public final void mUnicodeEscapeSequence() throws RecognitionException {
        try {
            // ES3.g3:789:2: ( 'u' HexDigit HexDigit HexDigit HexDigit )
            // ES3.g3:789:4: 'u' HexDigit HexDigit HexDigit HexDigit
            {
            match('u'); 
            mHexDigit(); 
            mHexDigit(); 
            mHexDigit(); 
            mHexDigit(); 

            }

        }
        finally {
        }
    }
    // $ANTLR end UnicodeEscapeSequence

    // $ANTLR start EscapeSequence
    public final void mEscapeSequence() throws RecognitionException {
        try {
            // ES3.g3:793:2: ( BSLASH ( CharacterEscapeSequence | OctalEscapeSequence | HexEscapeSequence | UnicodeEscapeSequence ) )
            // ES3.g3:794:2: BSLASH ( CharacterEscapeSequence | OctalEscapeSequence | HexEscapeSequence | UnicodeEscapeSequence )
            {
            mBSLASH(); 
            // ES3.g3:795:2: ( CharacterEscapeSequence | OctalEscapeSequence | HexEscapeSequence | UnicodeEscapeSequence )
            int alt24=4;
            int LA24_0 = input.LA(1);

            if ( ((LA24_0>='\u0000' && LA24_0<='\t')||(LA24_0>='\u000B' && LA24_0<='\f')||(LA24_0>='\u000E' && LA24_0<='/')||(LA24_0>=':' && LA24_0<='t')||(LA24_0>='v' && LA24_0<='w')||(LA24_0>='y' && LA24_0<='\u2027')||(LA24_0>='\u202A' && LA24_0<='\uFFFE')) ) {
                alt24=1;
            }
            else if ( ((LA24_0>='0' && LA24_0<='7')) ) {
                alt24=2;
            }
            else if ( (LA24_0=='x') ) {
                alt24=3;
            }
            else if ( (LA24_0=='u') ) {
                alt24=4;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("795:2: ( CharacterEscapeSequence | OctalEscapeSequence | HexEscapeSequence | UnicodeEscapeSequence )", 24, 0, input);

                throw nvae;
            }
            switch (alt24) {
                case 1 :
                    // ES3.g3:796:3: CharacterEscapeSequence
                    {
                    mCharacterEscapeSequence(); 

                    }
                    break;
                case 2 :
                    // ES3.g3:797:5: OctalEscapeSequence
                    {
                    mOctalEscapeSequence(); 

                    }
                    break;
                case 3 :
                    // ES3.g3:798:5: HexEscapeSequence
                    {
                    mHexEscapeSequence(); 

                    }
                    break;
                case 4 :
                    // ES3.g3:799:5: UnicodeEscapeSequence
                    {
                    mUnicodeEscapeSequence(); 

                    }
                    break;

            }


            }

        }
        finally {
        }
    }
    // $ANTLR end EscapeSequence

    // $ANTLR start StringLiteral
    public final void mStringLiteral() throws RecognitionException {
        try {
            int _type = StringLiteral;
            // ES3.g3:804:2: ( SQUOTE (~ ( SQUOTE | BSLASH | LineTerminator ) | EscapeSequence )* SQUOTE | DQUOTE (~ ( DQUOTE | BSLASH | LineTerminator ) | EscapeSequence )* DQUOTE )
            int alt27=2;
            int LA27_0 = input.LA(1);

            if ( (LA27_0=='\'') ) {
                alt27=1;
            }
            else if ( (LA27_0=='\"') ) {
                alt27=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("803:1: StringLiteral : ( SQUOTE (~ ( SQUOTE | BSLASH | LineTerminator ) | EscapeSequence )* SQUOTE | DQUOTE (~ ( DQUOTE | BSLASH | LineTerminator ) | EscapeSequence )* DQUOTE );", 27, 0, input);

                throw nvae;
            }
            switch (alt27) {
                case 1 :
                    // ES3.g3:804:4: SQUOTE (~ ( SQUOTE | BSLASH | LineTerminator ) | EscapeSequence )* SQUOTE
                    {
                    mSQUOTE(); 
                    // ES3.g3:804:11: (~ ( SQUOTE | BSLASH | LineTerminator ) | EscapeSequence )*
                    loop25:
                    do {
                        int alt25=3;
                        int LA25_0 = input.LA(1);

                        if ( ((LA25_0>='\u0000' && LA25_0<='\t')||(LA25_0>='\u000B' && LA25_0<='\f')||(LA25_0>='\u000E' && LA25_0<='&')||(LA25_0>='(' && LA25_0<='[')||(LA25_0>=']' && LA25_0<='\u2027')||(LA25_0>='\u202A' && LA25_0<='\uFFFE')) ) {
                            alt25=1;
                        }
                        else if ( (LA25_0=='\\') ) {
                            alt25=2;
                        }


                        switch (alt25) {
                    	case 1 :
                    	    // ES3.g3:804:13: ~ ( SQUOTE | BSLASH | LineTerminator )
                    	    {
                    	    if ( (input.LA(1)>='\u0000' && input.LA(1)<='\t')||(input.LA(1)>='\u000B' && input.LA(1)<='\f')||(input.LA(1)>='\u000E' && input.LA(1)<='&')||(input.LA(1)>='(' && input.LA(1)<='[')||(input.LA(1)>=']' && input.LA(1)<='\u2027')||(input.LA(1)>='\u202A' && input.LA(1)<='\uFFFE') ) {
                    	        input.consume();

                    	    }
                    	    else {
                    	        MismatchedSetException mse =
                    	            new MismatchedSetException(null,input);
                    	        recover(mse);    throw mse;
                    	    }


                    	    }
                    	    break;
                    	case 2 :
                    	    // ES3.g3:804:53: EscapeSequence
                    	    {
                    	    mEscapeSequence(); 

                    	    }
                    	    break;

                    	default :
                    	    break loop25;
                        }
                    } while (true);

                    mSQUOTE(); 

                    }
                    break;
                case 2 :
                    // ES3.g3:805:4: DQUOTE (~ ( DQUOTE | BSLASH | LineTerminator ) | EscapeSequence )* DQUOTE
                    {
                    mDQUOTE(); 
                    // ES3.g3:805:11: (~ ( DQUOTE | BSLASH | LineTerminator ) | EscapeSequence )*
                    loop26:
                    do {
                        int alt26=3;
                        int LA26_0 = input.LA(1);

                        if ( ((LA26_0>='\u0000' && LA26_0<='\t')||(LA26_0>='\u000B' && LA26_0<='\f')||(LA26_0>='\u000E' && LA26_0<='!')||(LA26_0>='#' && LA26_0<='[')||(LA26_0>=']' && LA26_0<='\u2027')||(LA26_0>='\u202A' && LA26_0<='\uFFFE')) ) {
                            alt26=1;
                        }
                        else if ( (LA26_0=='\\') ) {
                            alt26=2;
                        }


                        switch (alt26) {
                    	case 1 :
                    	    // ES3.g3:805:13: ~ ( DQUOTE | BSLASH | LineTerminator )
                    	    {
                    	    if ( (input.LA(1)>='\u0000' && input.LA(1)<='\t')||(input.LA(1)>='\u000B' && input.LA(1)<='\f')||(input.LA(1)>='\u000E' && input.LA(1)<='!')||(input.LA(1)>='#' && input.LA(1)<='[')||(input.LA(1)>=']' && input.LA(1)<='\u2027')||(input.LA(1)>='\u202A' && input.LA(1)<='\uFFFE') ) {
                    	        input.consume();

                    	    }
                    	    else {
                    	        MismatchedSetException mse =
                    	            new MismatchedSetException(null,input);
                    	        recover(mse);    throw mse;
                    	    }


                    	    }
                    	    break;
                    	case 2 :
                    	    // ES3.g3:805:53: EscapeSequence
                    	    {
                    	    mEscapeSequence(); 

                    	    }
                    	    break;

                    	default :
                    	    break loop26;
                        }
                    } while (true);

                    mDQUOTE(); 

                    }
                    break;

            }
            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end StringLiteral

    // $ANTLR start BackslashSequence
    public final void mBackslashSequence() throws RecognitionException {
        try {
            // ES3.g3:813:2: ( BSLASH ~ ( LineTerminator ) )
            // ES3.g3:813:4: BSLASH ~ ( LineTerminator )
            {
            mBSLASH(); 
            if ( (input.LA(1)>='\u0000' && input.LA(1)<='\t')||(input.LA(1)>='\u000B' && input.LA(1)<='\f')||(input.LA(1)>='\u000E' && input.LA(1)<='\u2027')||(input.LA(1)>='\u202A' && input.LA(1)<='\uFFFE') ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }


            }

        }
        finally {
        }
    }
    // $ANTLR end BackslashSequence

    // $ANTLR start RegularExpressionFirstChar
    public final void mRegularExpressionFirstChar() throws RecognitionException {
        try {
            // ES3.g3:817:2: (~ ( LineTerminator | MUL | BSLASH | DIV ) | BackslashSequence )
            int alt28=2;
            int LA28_0 = input.LA(1);

            if ( ((LA28_0>='\u0000' && LA28_0<='\t')||(LA28_0>='\u000B' && LA28_0<='\f')||(LA28_0>='\u000E' && LA28_0<=')')||(LA28_0>='+' && LA28_0<='.')||(LA28_0>='0' && LA28_0<='[')||(LA28_0>=']' && LA28_0<='\u2027')||(LA28_0>='\u202A' && LA28_0<='\uFFFE')) ) {
                alt28=1;
            }
            else if ( (LA28_0=='\\') ) {
                alt28=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("816:10: fragment RegularExpressionFirstChar : (~ ( LineTerminator | MUL | BSLASH | DIV ) | BackslashSequence );", 28, 0, input);

                throw nvae;
            }
            switch (alt28) {
                case 1 :
                    // ES3.g3:817:4: ~ ( LineTerminator | MUL | BSLASH | DIV )
                    {
                    if ( (input.LA(1)>='\u0000' && input.LA(1)<='\t')||(input.LA(1)>='\u000B' && input.LA(1)<='\f')||(input.LA(1)>='\u000E' && input.LA(1)<=')')||(input.LA(1)>='+' && input.LA(1)<='.')||(input.LA(1)>='0' && input.LA(1)<='[')||(input.LA(1)>=']' && input.LA(1)<='\u2027')||(input.LA(1)>='\u202A' && input.LA(1)<='\uFFFE') ) {
                        input.consume();

                    }
                    else {
                        MismatchedSetException mse =
                            new MismatchedSetException(null,input);
                        recover(mse);    throw mse;
                    }


                    }
                    break;
                case 2 :
                    // ES3.g3:818:4: BackslashSequence
                    {
                    mBackslashSequence(); 

                    }
                    break;

            }
        }
        finally {
        }
    }
    // $ANTLR end RegularExpressionFirstChar

    // $ANTLR start RegularExpressionChar
    public final void mRegularExpressionChar() throws RecognitionException {
        try {
            // ES3.g3:822:2: (~ ( LineTerminator | BSLASH | DIV ) | BackslashSequence )
            int alt29=2;
            int LA29_0 = input.LA(1);

            if ( ((LA29_0>='\u0000' && LA29_0<='\t')||(LA29_0>='\u000B' && LA29_0<='\f')||(LA29_0>='\u000E' && LA29_0<='.')||(LA29_0>='0' && LA29_0<='[')||(LA29_0>=']' && LA29_0<='\u2027')||(LA29_0>='\u202A' && LA29_0<='\uFFFE')) ) {
                alt29=1;
            }
            else if ( (LA29_0=='\\') ) {
                alt29=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("821:10: fragment RegularExpressionChar : (~ ( LineTerminator | BSLASH | DIV ) | BackslashSequence );", 29, 0, input);

                throw nvae;
            }
            switch (alt29) {
                case 1 :
                    // ES3.g3:822:4: ~ ( LineTerminator | BSLASH | DIV )
                    {
                    if ( (input.LA(1)>='\u0000' && input.LA(1)<='\t')||(input.LA(1)>='\u000B' && input.LA(1)<='\f')||(input.LA(1)>='\u000E' && input.LA(1)<='.')||(input.LA(1)>='0' && input.LA(1)<='[')||(input.LA(1)>=']' && input.LA(1)<='\u2027')||(input.LA(1)>='\u202A' && input.LA(1)<='\uFFFE') ) {
                        input.consume();

                    }
                    else {
                        MismatchedSetException mse =
                            new MismatchedSetException(null,input);
                        recover(mse);    throw mse;
                    }


                    }
                    break;
                case 2 :
                    // ES3.g3:823:4: BackslashSequence
                    {
                    mBackslashSequence(); 

                    }
                    break;

            }
        }
        finally {
        }
    }
    // $ANTLR end RegularExpressionChar

    // $ANTLR start RegularExpressionLiteral
    public final void mRegularExpressionLiteral() throws RecognitionException {
        try {
            int _type = RegularExpressionLiteral;
            // ES3.g3:827:2: ({...}? => DIV RegularExpressionFirstChar ( RegularExpressionChar )* DIV ( IdentifierPart )* )
            // ES3.g3:827:4: {...}? => DIV RegularExpressionFirstChar ( RegularExpressionChar )* DIV ( IdentifierPart )*
            {
            if ( !( areRegularExpressionsEnabled() ) ) {
                throw new FailedPredicateException(input, "RegularExpressionLiteral", " areRegularExpressionsEnabled() ");
            }
            mDIV(); 
            mRegularExpressionFirstChar(); 
            // ES3.g3:827:73: ( RegularExpressionChar )*
            loop30:
            do {
                int alt30=2;
                int LA30_0 = input.LA(1);

                if ( ((LA30_0>='\u0000' && LA30_0<='\t')||(LA30_0>='\u000B' && LA30_0<='\f')||(LA30_0>='\u000E' && LA30_0<='.')||(LA30_0>='0' && LA30_0<='\u2027')||(LA30_0>='\u202A' && LA30_0<='\uFFFE')) ) {
                    alt30=1;
                }


                switch (alt30) {
            	case 1 :
            	    // ES3.g3:827:73: RegularExpressionChar
            	    {
            	    mRegularExpressionChar(); 

            	    }
            	    break;

            	default :
            	    break loop30;
                }
            } while (true);

            mDIV(); 
            // ES3.g3:827:100: ( IdentifierPart )*
            loop31:
            do {
                int alt31=2;
                int LA31_0 = input.LA(1);

                if ( (LA31_0=='$'||(LA31_0>='0' && LA31_0<='9')||(LA31_0>='A' && LA31_0<='Z')||LA31_0=='\\'||LA31_0=='_'||(LA31_0>='a' && LA31_0<='z')) ) {
                    alt31=1;
                }
                else if ( ( isIdentifierPartUnicode(input.LA(1)) ) ) {
                    alt31=1;
                }


                switch (alt31) {
            	case 1 :
            	    // ES3.g3:827:100: IdentifierPart
            	    {
            	    mIdentifierPart(); 

            	    }
            	    break;

            	default :
            	    break loop31;
                }
            } while (true);


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end RegularExpressionLiteral

    public void mTokens() throws RecognitionException {
        // ES3.g3:1:8: ( NULL | TRUE | FALSE | BREAK | CASE | CATCH | CONTINUE | DEFAULT | DELETE | DO | ELSE | FINALLY | FOR | FUNCTION | IF | IN | INSTANCEOF | NEW | RETURN | SWITCH | THIS | THROW | TRY | TYPEOF | VAR | VOID | WHILE | WITH | ABSTRACT | BOOLEAN | BYTE | CHAR | CLASS | CONST | DEBUGGER | DOUBLE | ENUM | EXPORT | EXTENDS | FINAL | FLOAT | GOTO | IMPLEMENTS | IMPORT | INT | INTERFACE | LONG | NATIVE | PACKAGE | PRIVATE | PROTECTED | PUBLIC | SHORT | STATIC | SUPER | SYNCHRONIZED | THROWS | TRANSIENT | VOLATILE | LBRACE | RBRACE | LPAREN | RPAREN | LBRACK | RBRACK | DOT | SEMIC | COMMA | LT | GT | LTE | GTE | EQ | NEQ | SAME | NSAME | ADD | SUB | MUL | MOD | INC | DEC | SHL | SHR | SHU | AND | OR | XOR | NOT | INV | LAND | LOR | QUE | COLON | ASSIGN | ADDASS | SUBASS | MULASS | MODASS | SHLASS | SHRASS | SHUASS | ANDASS | ORASS | XORASS | DIV | DIVASS | WhiteSpace | EOL | MultiLineComment | SingleLineComment | Identifier | DecimalLiteral | OctalIntegerLiteral | HexIntegerLiteral | StringLiteral | RegularExpressionLiteral )
        int alt32=117;
        switch ( input.LA(1) ) {
        case 'n':
            {
            switch ( input.LA(2) ) {
            case 'a':
                {
                int LA32_47 = input.LA(3);

                if ( (LA32_47=='t') ) {
                    int LA32_125 = input.LA(4);

                    if ( (LA32_125=='i') ) {
                        int LA32_189 = input.LA(5);

                        if ( (LA32_189=='v') ) {
                            int LA32_245 = input.LA(6);

                            if ( (LA32_245=='e') ) {
                                int LA32_294 = input.LA(7);

                                if ( (LA32_294=='$'||(LA32_294>='0' && LA32_294<='9')||(LA32_294>='A' && LA32_294<='Z')||LA32_294=='\\'||LA32_294=='_'||(LA32_294>='a' && LA32_294<='z')) ) {
                                    alt32=112;
                                }
                                else {
                                    alt32=48;}
                            }
                            else {
                                alt32=112;}
                        }
                        else {
                            alt32=112;}
                    }
                    else {
                        alt32=112;}
                }
                else {
                    alt32=112;}
                }
                break;
            case 'u':
                {
                int LA32_48 = input.LA(3);

                if ( (LA32_48=='l') ) {
                    int LA32_126 = input.LA(4);

                    if ( (LA32_126=='l') ) {
                        int LA32_190 = input.LA(5);

                        if ( (LA32_190=='$'||(LA32_190>='0' && LA32_190<='9')||(LA32_190>='A' && LA32_190<='Z')||LA32_190=='\\'||LA32_190=='_'||(LA32_190>='a' && LA32_190<='z')) ) {
                            alt32=112;
                        }
                        else {
                            alt32=1;}
                    }
                    else {
                        alt32=112;}
                }
                else {
                    alt32=112;}
                }
                break;
            case 'e':
                {
                int LA32_49 = input.LA(3);

                if ( (LA32_49=='w') ) {
                    int LA32_127 = input.LA(4);

                    if ( (LA32_127=='$'||(LA32_127>='0' && LA32_127<='9')||(LA32_127>='A' && LA32_127<='Z')||LA32_127=='\\'||LA32_127=='_'||(LA32_127>='a' && LA32_127<='z')) ) {
                        alt32=112;
                    }
                    else {
                        alt32=18;}
                }
                else {
                    alt32=112;}
                }
                break;
            default:
                alt32=112;}

            }
            break;
        case 't':
            {
            switch ( input.LA(2) ) {
            case 'r':
                {
                switch ( input.LA(3) ) {
                case 'a':
                    {
                    int LA32_128 = input.LA(4);

                    if ( (LA32_128=='n') ) {
                        int LA32_192 = input.LA(5);

                        if ( (LA32_192=='s') ) {
                            int LA32_247 = input.LA(6);

                            if ( (LA32_247=='i') ) {
                                int LA32_295 = input.LA(7);

                                if ( (LA32_295=='e') ) {
                                    int LA32_334 = input.LA(8);

                                    if ( (LA32_334=='n') ) {
                                        int LA32_361 = input.LA(9);

                                        if ( (LA32_361=='t') ) {
                                            int LA32_378 = input.LA(10);

                                            if ( (LA32_378=='$'||(LA32_378>='0' && LA32_378<='9')||(LA32_378>='A' && LA32_378<='Z')||LA32_378=='\\'||LA32_378=='_'||(LA32_378>='a' && LA32_378<='z')) ) {
                                                alt32=112;
                                            }
                                            else {
                                                alt32=58;}
                                        }
                                        else {
                                            alt32=112;}
                                    }
                                    else {
                                        alt32=112;}
                                }
                                else {
                                    alt32=112;}
                            }
                            else {
                                alt32=112;}
                        }
                        else {
                            alt32=112;}
                    }
                    else {
                        alt32=112;}
                    }
                    break;
                case 'u':
                    {
                    int LA32_129 = input.LA(4);

                    if ( (LA32_129=='e') ) {
                        int LA32_193 = input.LA(5);

                        if ( (LA32_193=='$'||(LA32_193>='0' && LA32_193<='9')||(LA32_193>='A' && LA32_193<='Z')||LA32_193=='\\'||LA32_193=='_'||(LA32_193>='a' && LA32_193<='z')) ) {
                            alt32=112;
                        }
                        else {
                            alt32=2;}
                    }
                    else {
                        alt32=112;}
                    }
                    break;
                case 'y':
                    {
                    int LA32_130 = input.LA(4);

                    if ( (LA32_130=='$'||(LA32_130>='0' && LA32_130<='9')||(LA32_130>='A' && LA32_130<='Z')||LA32_130=='\\'||LA32_130=='_'||(LA32_130>='a' && LA32_130<='z')) ) {
                        alt32=112;
                    }
                    else {
                        alt32=23;}
                    }
                    break;
                default:
                    alt32=112;}

                }
                break;
            case 'h':
                {
                switch ( input.LA(3) ) {
                case 'r':
                    {
                    int LA32_131 = input.LA(4);

                    if ( (LA32_131=='o') ) {
                        int LA32_195 = input.LA(5);

                        if ( (LA32_195=='w') ) {
                            switch ( input.LA(6) ) {
                            case 's':
                                {
                                int LA32_296 = input.LA(7);

                                if ( (LA32_296=='$'||(LA32_296>='0' && LA32_296<='9')||(LA32_296>='A' && LA32_296<='Z')||LA32_296=='\\'||LA32_296=='_'||(LA32_296>='a' && LA32_296<='z')) ) {
                                    alt32=112;
                                }
                                else {
                                    alt32=57;}
                                }
                                break;
                            case '$':
                            case '0':
                            case '1':
                            case '2':
                            case '3':
                            case '4':
                            case '5':
                            case '6':
                            case '7':
                            case '8':
                            case '9':
                            case 'A':
                            case 'B':
                            case 'C':
                            case 'D':
                            case 'E':
                            case 'F':
                            case 'G':
                            case 'H':
                            case 'I':
                            case 'J':
                            case 'K':
                            case 'L':
                            case 'M':
                            case 'N':
                            case 'O':
                            case 'P':
                            case 'Q':
                            case 'R':
                            case 'S':
                            case 'T':
                            case 'U':
                            case 'V':
                            case 'W':
                            case 'X':
                            case 'Y':
                            case 'Z':
                            case '\\':
                            case '_':
                            case 'a':
                            case 'b':
                            case 'c':
                            case 'd':
                            case 'e':
                            case 'f':
                            case 'g':
                            case 'h':
                            case 'i':
                            case 'j':
                            case 'k':
                            case 'l':
                            case 'm':
                            case 'n':
                            case 'o':
                            case 'p':
                            case 'q':
                            case 'r':
                            case 't':
                            case 'u':
                            case 'v':
                            case 'w':
                            case 'x':
                            case 'y':
                            case 'z':
                                {
                                alt32=112;
                                }
                                break;
                            default:
                                alt32=22;}

                        }
                        else {
                            alt32=112;}
                    }
                    else {
                        alt32=112;}
                    }
                    break;
                case 'i':
                    {
                    int LA32_132 = input.LA(4);

                    if ( (LA32_132=='s') ) {
                        int LA32_196 = input.LA(5);

                        if ( (LA32_196=='$'||(LA32_196>='0' && LA32_196<='9')||(LA32_196>='A' && LA32_196<='Z')||LA32_196=='\\'||LA32_196=='_'||(LA32_196>='a' && LA32_196<='z')) ) {
                            alt32=112;
                        }
                        else {
                            alt32=21;}
                    }
                    else {
                        alt32=112;}
                    }
                    break;
                default:
                    alt32=112;}

                }
                break;
            case 'y':
                {
                int LA32_52 = input.LA(3);

                if ( (LA32_52=='p') ) {
                    int LA32_133 = input.LA(4);

                    if ( (LA32_133=='e') ) {
                        int LA32_197 = input.LA(5);

                        if ( (LA32_197=='o') ) {
                            int LA32_251 = input.LA(6);

                            if ( (LA32_251=='f') ) {
                                int LA32_298 = input.LA(7);

                                if ( (LA32_298=='$'||(LA32_298>='0' && LA32_298<='9')||(LA32_298>='A' && LA32_298<='Z')||LA32_298=='\\'||LA32_298=='_'||(LA32_298>='a' && LA32_298<='z')) ) {
                                    alt32=112;
                                }
                                else {
                                    alt32=24;}
                            }
                            else {
                                alt32=112;}
                        }
                        else {
                            alt32=112;}
                    }
                    else {
                        alt32=112;}
                }
                else {
                    alt32=112;}
                }
                break;
            default:
                alt32=112;}

            }
            break;
        case 'f':
            {
            switch ( input.LA(2) ) {
            case 'a':
                {
                int LA32_53 = input.LA(3);

                if ( (LA32_53=='l') ) {
                    int LA32_134 = input.LA(4);

                    if ( (LA32_134=='s') ) {
                        int LA32_198 = input.LA(5);

                        if ( (LA32_198=='e') ) {
                            int LA32_252 = input.LA(6);

                            if ( (LA32_252=='$'||(LA32_252>='0' && LA32_252<='9')||(LA32_252>='A' && LA32_252<='Z')||LA32_252=='\\'||LA32_252=='_'||(LA32_252>='a' && LA32_252<='z')) ) {
                                alt32=112;
                            }
                            else {
                                alt32=3;}
                        }
                        else {
                            alt32=112;}
                    }
                    else {
                        alt32=112;}
                }
                else {
                    alt32=112;}
                }
                break;
            case 'u':
                {
                int LA32_54 = input.LA(3);

                if ( (LA32_54=='n') ) {
                    int LA32_135 = input.LA(4);

                    if ( (LA32_135=='c') ) {
                        int LA32_199 = input.LA(5);

                        if ( (LA32_199=='t') ) {
                            int LA32_253 = input.LA(6);

                            if ( (LA32_253=='i') ) {
                                int LA32_300 = input.LA(7);

                                if ( (LA32_300=='o') ) {
                                    int LA32_337 = input.LA(8);

                                    if ( (LA32_337=='n') ) {
                                        int LA32_362 = input.LA(9);

                                        if ( (LA32_362=='$'||(LA32_362>='0' && LA32_362<='9')||(LA32_362>='A' && LA32_362<='Z')||LA32_362=='\\'||LA32_362=='_'||(LA32_362>='a' && LA32_362<='z')) ) {
                                            alt32=112;
                                        }
                                        else {
                                            alt32=14;}
                                    }
                                    else {
                                        alt32=112;}
                                }
                                else {
                                    alt32=112;}
                            }
                            else {
                                alt32=112;}
                        }
                        else {
                            alt32=112;}
                    }
                    else {
                        alt32=112;}
                }
                else {
                    alt32=112;}
                }
                break;
            case 'i':
                {
                int LA32_55 = input.LA(3);

                if ( (LA32_55=='n') ) {
                    int LA32_136 = input.LA(4);

                    if ( (LA32_136=='a') ) {
                        int LA32_200 = input.LA(5);

                        if ( (LA32_200=='l') ) {
                            switch ( input.LA(6) ) {
                            case 'l':
                                {
                                int LA32_301 = input.LA(7);

                                if ( (LA32_301=='y') ) {
                                    int LA32_338 = input.LA(8);

                                    if ( (LA32_338=='$'||(LA32_338>='0' && LA32_338<='9')||(LA32_338>='A' && LA32_338<='Z')||LA32_338=='\\'||LA32_338=='_'||(LA32_338>='a' && LA32_338<='z')) ) {
                                        alt32=112;
                                    }
                                    else {
                                        alt32=12;}
                                }
                                else {
                                    alt32=112;}
                                }
                                break;
                            case '$':
                            case '0':
                            case '1':
                            case '2':
                            case '3':
                            case '4':
                            case '5':
                            case '6':
                            case '7':
                            case '8':
                            case '9':
                            case 'A':
                            case 'B':
                            case 'C':
                            case 'D':
                            case 'E':
                            case 'F':
                            case 'G':
                            case 'H':
                            case 'I':
                            case 'J':
                            case 'K':
                            case 'L':
                            case 'M':
                            case 'N':
                            case 'O':
                            case 'P':
                            case 'Q':
                            case 'R':
                            case 'S':
                            case 'T':
                            case 'U':
                            case 'V':
                            case 'W':
                            case 'X':
                            case 'Y':
                            case 'Z':
                            case '\\':
                            case '_':
                            case 'a':
                            case 'b':
                            case 'c':
                            case 'd':
                            case 'e':
                            case 'f':
                            case 'g':
                            case 'h':
                            case 'i':
                            case 'j':
                            case 'k':
                            case 'm':
                            case 'n':
                            case 'o':
                            case 'p':
                            case 'q':
                            case 'r':
                            case 's':
                            case 't':
                            case 'u':
                            case 'v':
                            case 'w':
                            case 'x':
                            case 'y':
                            case 'z':
                                {
                                alt32=112;
                                }
                                break;
                            default:
                                alt32=40;}

                        }
                        else {
                            alt32=112;}
                    }
                    else {
                        alt32=112;}
                }
                else {
                    alt32=112;}
                }
                break;
            case 'o':
                {
                int LA32_56 = input.LA(3);

                if ( (LA32_56=='r') ) {
                    int LA32_137 = input.LA(4);

                    if ( (LA32_137=='$'||(LA32_137>='0' && LA32_137<='9')||(LA32_137>='A' && LA32_137<='Z')||LA32_137=='\\'||LA32_137=='_'||(LA32_137>='a' && LA32_137<='z')) ) {
                        alt32=112;
                    }
                    else {
                        alt32=13;}
                }
                else {
                    alt32=112;}
                }
                break;
            case 'l':
                {
                int LA32_57 = input.LA(3);

                if ( (LA32_57=='o') ) {
                    int LA32_138 = input.LA(4);

                    if ( (LA32_138=='a') ) {
                        int LA32_202 = input.LA(5);

                        if ( (LA32_202=='t') ) {
                            int LA32_255 = input.LA(6);

                            if ( (LA32_255=='$'||(LA32_255>='0' && LA32_255<='9')||(LA32_255>='A' && LA32_255<='Z')||LA32_255=='\\'||LA32_255=='_'||(LA32_255>='a' && LA32_255<='z')) ) {
                                alt32=112;
                            }
                            else {
                                alt32=41;}
                        }
                        else {
                            alt32=112;}
                    }
                    else {
                        alt32=112;}
                }
                else {
                    alt32=112;}
                }
                break;
            default:
                alt32=112;}

            }
            break;
        case 'b':
            {
            switch ( input.LA(2) ) {
            case 'r':
                {
                int LA32_58 = input.LA(3);

                if ( (LA32_58=='e') ) {
                    int LA32_139 = input.LA(4);

                    if ( (LA32_139=='a') ) {
                        int LA32_203 = input.LA(5);

                        if ( (LA32_203=='k') ) {
                            int LA32_256 = input.LA(6);

                            if ( (LA32_256=='$'||(LA32_256>='0' && LA32_256<='9')||(LA32_256>='A' && LA32_256<='Z')||LA32_256=='\\'||LA32_256=='_'||(LA32_256>='a' && LA32_256<='z')) ) {
                                alt32=112;
                            }
                            else {
                                alt32=4;}
                        }
                        else {
                            alt32=112;}
                    }
                    else {
                        alt32=112;}
                }
                else {
                    alt32=112;}
                }
                break;
            case 'y':
                {
                int LA32_59 = input.LA(3);

                if ( (LA32_59=='t') ) {
                    int LA32_140 = input.LA(4);

                    if ( (LA32_140=='e') ) {
                        int LA32_204 = input.LA(5);

                        if ( (LA32_204=='$'||(LA32_204>='0' && LA32_204<='9')||(LA32_204>='A' && LA32_204<='Z')||LA32_204=='\\'||LA32_204=='_'||(LA32_204>='a' && LA32_204<='z')) ) {
                            alt32=112;
                        }
                        else {
                            alt32=31;}
                    }
                    else {
                        alt32=112;}
                }
                else {
                    alt32=112;}
                }
                break;
            case 'o':
                {
                int LA32_60 = input.LA(3);

                if ( (LA32_60=='o') ) {
                    int LA32_141 = input.LA(4);

                    if ( (LA32_141=='l') ) {
                        int LA32_205 = input.LA(5);

                        if ( (LA32_205=='e') ) {
                            int LA32_258 = input.LA(6);

                            if ( (LA32_258=='a') ) {
                                int LA32_305 = input.LA(7);

                                if ( (LA32_305=='n') ) {
                                    int LA32_339 = input.LA(8);

                                    if ( (LA32_339=='$'||(LA32_339>='0' && LA32_339<='9')||(LA32_339>='A' && LA32_339<='Z')||LA32_339=='\\'||LA32_339=='_'||(LA32_339>='a' && LA32_339<='z')) ) {
                                        alt32=112;
                                    }
                                    else {
                                        alt32=30;}
                                }
                                else {
                                    alt32=112;}
                            }
                            else {
                                alt32=112;}
                        }
                        else {
                            alt32=112;}
                    }
                    else {
                        alt32=112;}
                }
                else {
                    alt32=112;}
                }
                break;
            default:
                alt32=112;}

            }
            break;
        case 'c':
            {
            switch ( input.LA(2) ) {
            case 'a':
                {
                switch ( input.LA(3) ) {
                case 's':
                    {
                    int LA32_142 = input.LA(4);

                    if ( (LA32_142=='e') ) {
                        int LA32_206 = input.LA(5);

                        if ( (LA32_206=='$'||(LA32_206>='0' && LA32_206<='9')||(LA32_206>='A' && LA32_206<='Z')||LA32_206=='\\'||LA32_206=='_'||(LA32_206>='a' && LA32_206<='z')) ) {
                            alt32=112;
                        }
                        else {
                            alt32=5;}
                    }
                    else {
                        alt32=112;}
                    }
                    break;
                case 't':
                    {
                    int LA32_143 = input.LA(4);

                    if ( (LA32_143=='c') ) {
                        int LA32_207 = input.LA(5);

                        if ( (LA32_207=='h') ) {
                            int LA32_260 = input.LA(6);

                            if ( (LA32_260=='$'||(LA32_260>='0' && LA32_260<='9')||(LA32_260>='A' && LA32_260<='Z')||LA32_260=='\\'||LA32_260=='_'||(LA32_260>='a' && LA32_260<='z')) ) {
                                alt32=112;
                            }
                            else {
                                alt32=6;}
                        }
                        else {
                            alt32=112;}
                    }
                    else {
                        alt32=112;}
                    }
                    break;
                default:
                    alt32=112;}

                }
                break;
            case 'o':
                {
                int LA32_62 = input.LA(3);

                if ( (LA32_62=='n') ) {
                    switch ( input.LA(4) ) {
                    case 't':
                        {
                        int LA32_208 = input.LA(5);

                        if ( (LA32_208=='i') ) {
                            int LA32_261 = input.LA(6);

                            if ( (LA32_261=='n') ) {
                                int LA32_307 = input.LA(7);

                                if ( (LA32_307=='u') ) {
                                    int LA32_340 = input.LA(8);

                                    if ( (LA32_340=='e') ) {
                                        int LA32_365 = input.LA(9);

                                        if ( (LA32_365=='$'||(LA32_365>='0' && LA32_365<='9')||(LA32_365>='A' && LA32_365<='Z')||LA32_365=='\\'||LA32_365=='_'||(LA32_365>='a' && LA32_365<='z')) ) {
                                            alt32=112;
                                        }
                                        else {
                                            alt32=7;}
                                    }
                                    else {
                                        alt32=112;}
                                }
                                else {
                                    alt32=112;}
                            }
                            else {
                                alt32=112;}
                        }
                        else {
                            alt32=112;}
                        }
                        break;
                    case 's':
                        {
                        int LA32_209 = input.LA(5);

                        if ( (LA32_209=='t') ) {
                            int LA32_262 = input.LA(6);

                            if ( (LA32_262=='$'||(LA32_262>='0' && LA32_262<='9')||(LA32_262>='A' && LA32_262<='Z')||LA32_262=='\\'||LA32_262=='_'||(LA32_262>='a' && LA32_262<='z')) ) {
                                alt32=112;
                            }
                            else {
                                alt32=34;}
                        }
                        else {
                            alt32=112;}
                        }
                        break;
                    default:
                        alt32=112;}

                }
                else {
                    alt32=112;}
                }
                break;
            case 'h':
                {
                int LA32_63 = input.LA(3);

                if ( (LA32_63=='a') ) {
                    int LA32_145 = input.LA(4);

                    if ( (LA32_145=='r') ) {
                        int LA32_210 = input.LA(5);

                        if ( (LA32_210=='$'||(LA32_210>='0' && LA32_210<='9')||(LA32_210>='A' && LA32_210<='Z')||LA32_210=='\\'||LA32_210=='_'||(LA32_210>='a' && LA32_210<='z')) ) {
                            alt32=112;
                        }
                        else {
                            alt32=32;}
                    }
                    else {
                        alt32=112;}
                }
                else {
                    alt32=112;}
                }
                break;
            case 'l':
                {
                int LA32_64 = input.LA(3);

                if ( (LA32_64=='a') ) {
                    int LA32_146 = input.LA(4);

                    if ( (LA32_146=='s') ) {
                        int LA32_211 = input.LA(5);

                        if ( (LA32_211=='s') ) {
                            int LA32_264 = input.LA(6);

                            if ( (LA32_264=='$'||(LA32_264>='0' && LA32_264<='9')||(LA32_264>='A' && LA32_264<='Z')||LA32_264=='\\'||LA32_264=='_'||(LA32_264>='a' && LA32_264<='z')) ) {
                                alt32=112;
                            }
                            else {
                                alt32=33;}
                        }
                        else {
                            alt32=112;}
                    }
                    else {
                        alt32=112;}
                }
                else {
                    alt32=112;}
                }
                break;
            default:
                alt32=112;}

            }
            break;
        case 'd':
            {
            switch ( input.LA(2) ) {
            case 'o':
                {
                switch ( input.LA(3) ) {
                case 'u':
                    {
                    int LA32_147 = input.LA(4);

                    if ( (LA32_147=='b') ) {
                        int LA32_212 = input.LA(5);

                        if ( (LA32_212=='l') ) {
                            int LA32_265 = input.LA(6);

                            if ( (LA32_265=='e') ) {
                                int LA32_310 = input.LA(7);

                                if ( (LA32_310=='$'||(LA32_310>='0' && LA32_310<='9')||(LA32_310>='A' && LA32_310<='Z')||LA32_310=='\\'||LA32_310=='_'||(LA32_310>='a' && LA32_310<='z')) ) {
                                    alt32=112;
                                }
                                else {
                                    alt32=36;}
                            }
                            else {
                                alt32=112;}
                        }
                        else {
                            alt32=112;}
                    }
                    else {
                        alt32=112;}
                    }
                    break;
                case '$':
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                case 'A':
                case 'B':
                case 'C':
                case 'D':
                case 'E':
                case 'F':
                case 'G':
                case 'H':
                case 'I':
                case 'J':
                case 'K':
                case 'L':
                case 'M':
                case 'N':
                case 'O':
                case 'P':
                case 'Q':
                case 'R':
                case 'S':
                case 'T':
                case 'U':
                case 'V':
                case 'W':
                case 'X':
                case 'Y':
                case 'Z':
                case '\\':
                case '_':
                case 'a':
                case 'b':
                case 'c':
                case 'd':
                case 'e':
                case 'f':
                case 'g':
                case 'h':
                case 'i':
                case 'j':
                case 'k':
                case 'l':
                case 'm':
                case 'n':
                case 'o':
                case 'p':
                case 'q':
                case 'r':
                case 's':
                case 't':
                case 'v':
                case 'w':
                case 'x':
                case 'y':
                case 'z':
                    {
                    alt32=112;
                    }
                    break;
                default:
                    alt32=10;}

                }
                break;
            case 'e':
                {
                switch ( input.LA(3) ) {
                case 'f':
                    {
                    int LA32_149 = input.LA(4);

                    if ( (LA32_149=='a') ) {
                        int LA32_213 = input.LA(5);

                        if ( (LA32_213=='u') ) {
                            int LA32_266 = input.LA(6);

                            if ( (LA32_266=='l') ) {
                                int LA32_311 = input.LA(7);

                                if ( (LA32_311=='t') ) {
                                    int LA32_342 = input.LA(8);

                                    if ( (LA32_342=='$'||(LA32_342>='0' && LA32_342<='9')||(LA32_342>='A' && LA32_342<='Z')||LA32_342=='\\'||LA32_342=='_'||(LA32_342>='a' && LA32_342<='z')) ) {
                                        alt32=112;
                                    }
                                    else {
                                        alt32=8;}
                                }
                                else {
                                    alt32=112;}
                            }
                            else {
                                alt32=112;}
                        }
                        else {
                            alt32=112;}
                    }
                    else {
                        alt32=112;}
                    }
                    break;
                case 'b':
                    {
                    int LA32_150 = input.LA(4);

                    if ( (LA32_150=='u') ) {
                        int LA32_214 = input.LA(5);

                        if ( (LA32_214=='g') ) {
                            int LA32_267 = input.LA(6);

                            if ( (LA32_267=='g') ) {
                                int LA32_312 = input.LA(7);

                                if ( (LA32_312=='e') ) {
                                    int LA32_343 = input.LA(8);

                                    if ( (LA32_343=='r') ) {
                                        int LA32_367 = input.LA(9);

                                        if ( (LA32_367=='$'||(LA32_367>='0' && LA32_367<='9')||(LA32_367>='A' && LA32_367<='Z')||LA32_367=='\\'||LA32_367=='_'||(LA32_367>='a' && LA32_367<='z')) ) {
                                            alt32=112;
                                        }
                                        else {
                                            alt32=35;}
                                    }
                                    else {
                                        alt32=112;}
                                }
                                else {
                                    alt32=112;}
                            }
                            else {
                                alt32=112;}
                        }
                        else {
                            alt32=112;}
                    }
                    else {
                        alt32=112;}
                    }
                    break;
                case 'l':
                    {
                    int LA32_151 = input.LA(4);

                    if ( (LA32_151=='e') ) {
                        int LA32_215 = input.LA(5);

                        if ( (LA32_215=='t') ) {
                            int LA32_268 = input.LA(6);

                            if ( (LA32_268=='e') ) {
                                int LA32_313 = input.LA(7);

                                if ( (LA32_313=='$'||(LA32_313>='0' && LA32_313<='9')||(LA32_313>='A' && LA32_313<='Z')||LA32_313=='\\'||LA32_313=='_'||(LA32_313>='a' && LA32_313<='z')) ) {
                                    alt32=112;
                                }
                                else {
                                    alt32=9;}
                            }
                            else {
                                alt32=112;}
                        }
                        else {
                            alt32=112;}
                    }
                    else {
                        alt32=112;}
                    }
                    break;
                default:
                    alt32=112;}

                }
                break;
            default:
                alt32=112;}

            }
            break;
        case 'e':
            {
            switch ( input.LA(2) ) {
            case 'l':
                {
                int LA32_67 = input.LA(3);

                if ( (LA32_67=='s') ) {
                    int LA32_152 = input.LA(4);

                    if ( (LA32_152=='e') ) {
                        int LA32_216 = input.LA(5);

                        if ( (LA32_216=='$'||(LA32_216>='0' && LA32_216<='9')||(LA32_216>='A' && LA32_216<='Z')||LA32_216=='\\'||LA32_216=='_'||(LA32_216>='a' && LA32_216<='z')) ) {
                            alt32=112;
                        }
                        else {
                            alt32=11;}
                    }
                    else {
                        alt32=112;}
                }
                else {
                    alt32=112;}
                }
                break;
            case 'x':
                {
                switch ( input.LA(3) ) {
                case 'p':
                    {
                    int LA32_153 = input.LA(4);

                    if ( (LA32_153=='o') ) {
                        int LA32_217 = input.LA(5);

                        if ( (LA32_217=='r') ) {
                            int LA32_270 = input.LA(6);

                            if ( (LA32_270=='t') ) {
                                int LA32_314 = input.LA(7);

                                if ( (LA32_314=='$'||(LA32_314>='0' && LA32_314<='9')||(LA32_314>='A' && LA32_314<='Z')||LA32_314=='\\'||LA32_314=='_'||(LA32_314>='a' && LA32_314<='z')) ) {
                                    alt32=112;
                                }
                                else {
                                    alt32=38;}
                            }
                            else {
                                alt32=112;}
                        }
                        else {
                            alt32=112;}
                    }
                    else {
                        alt32=112;}
                    }
                    break;
                case 't':
                    {
                    int LA32_154 = input.LA(4);

                    if ( (LA32_154=='e') ) {
                        int LA32_218 = input.LA(5);

                        if ( (LA32_218=='n') ) {
                            int LA32_271 = input.LA(6);

                            if ( (LA32_271=='d') ) {
                                int LA32_315 = input.LA(7);

                                if ( (LA32_315=='s') ) {
                                    int LA32_346 = input.LA(8);

                                    if ( (LA32_346=='$'||(LA32_346>='0' && LA32_346<='9')||(LA32_346>='A' && LA32_346<='Z')||LA32_346=='\\'||LA32_346=='_'||(LA32_346>='a' && LA32_346<='z')) ) {
                                        alt32=112;
                                    }
                                    else {
                                        alt32=39;}
                                }
                                else {
                                    alt32=112;}
                            }
                            else {
                                alt32=112;}
                        }
                        else {
                            alt32=112;}
                    }
                    else {
                        alt32=112;}
                    }
                    break;
                default:
                    alt32=112;}

                }
                break;
            case 'n':
                {
                int LA32_69 = input.LA(3);

                if ( (LA32_69=='u') ) {
                    int LA32_155 = input.LA(4);

                    if ( (LA32_155=='m') ) {
                        int LA32_219 = input.LA(5);

                        if ( (LA32_219=='$'||(LA32_219>='0' && LA32_219<='9')||(LA32_219>='A' && LA32_219<='Z')||LA32_219=='\\'||LA32_219=='_'||(LA32_219>='a' && LA32_219<='z')) ) {
                            alt32=112;
                        }
                        else {
                            alt32=37;}
                    }
                    else {
                        alt32=112;}
                }
                else {
                    alt32=112;}
                }
                break;
            default:
                alt32=112;}

            }
            break;
        case 'i':
            {
            switch ( input.LA(2) ) {
            case 'n':
                {
                switch ( input.LA(3) ) {
                case 's':
                    {
                    int LA32_156 = input.LA(4);

                    if ( (LA32_156=='t') ) {
                        int LA32_220 = input.LA(5);

                        if ( (LA32_220=='a') ) {
                            int LA32_273 = input.LA(6);

                            if ( (LA32_273=='n') ) {
                                int LA32_316 = input.LA(7);

                                if ( (LA32_316=='c') ) {
                                    int LA32_347 = input.LA(8);

                                    if ( (LA32_347=='e') ) {
                                        int LA32_369 = input.LA(9);

                                        if ( (LA32_369=='o') ) {
                                            int LA32_382 = input.LA(10);

                                            if ( (LA32_382=='f') ) {
                                                int LA32_390 = input.LA(11);

                                                if ( (LA32_390=='$'||(LA32_390>='0' && LA32_390<='9')||(LA32_390>='A' && LA32_390<='Z')||LA32_390=='\\'||LA32_390=='_'||(LA32_390>='a' && LA32_390<='z')) ) {
                                                    alt32=112;
                                                }
                                                else {
                                                    alt32=17;}
                                            }
                                            else {
                                                alt32=112;}
                                        }
                                        else {
                                            alt32=112;}
                                    }
                                    else {
                                        alt32=112;}
                                }
                                else {
                                    alt32=112;}
                            }
                            else {
                                alt32=112;}
                        }
                        else {
                            alt32=112;}
                    }
                    else {
                        alt32=112;}
                    }
                    break;
                case 't':
                    {
                    switch ( input.LA(4) ) {
                    case 'e':
                        {
                        int LA32_221 = input.LA(5);

                        if ( (LA32_221=='r') ) {
                            int LA32_274 = input.LA(6);

                            if ( (LA32_274=='f') ) {
                                int LA32_317 = input.LA(7);

                                if ( (LA32_317=='a') ) {
                                    int LA32_348 = input.LA(8);

                                    if ( (LA32_348=='c') ) {
                                        int LA32_370 = input.LA(9);

                                        if ( (LA32_370=='e') ) {
                                            int LA32_383 = input.LA(10);

                                            if ( (LA32_383=='$'||(LA32_383>='0' && LA32_383<='9')||(LA32_383>='A' && LA32_383<='Z')||LA32_383=='\\'||LA32_383=='_'||(LA32_383>='a' && LA32_383<='z')) ) {
                                                alt32=112;
                                            }
                                            else {
                                                alt32=46;}
                                        }
                                        else {
                                            alt32=112;}
                                    }
                                    else {
                                        alt32=112;}
                                }
                                else {
                                    alt32=112;}
                            }
                            else {
                                alt32=112;}
                        }
                        else {
                            alt32=112;}
                        }
                        break;
                    case '$':
                    case '0':
                    case '1':
                    case '2':
                    case '3':
                    case '4':
                    case '5':
                    case '6':
                    case '7':
                    case '8':
                    case '9':
                    case 'A':
                    case 'B':
                    case 'C':
                    case 'D':
                    case 'E':
                    case 'F':
                    case 'G':
                    case 'H':
                    case 'I':
                    case 'J':
                    case 'K':
                    case 'L':
                    case 'M':
                    case 'N':
                    case 'O':
                    case 'P':
                    case 'Q':
                    case 'R':
                    case 'S':
                    case 'T':
                    case 'U':
                    case 'V':
                    case 'W':
                    case 'X':
                    case 'Y':
                    case 'Z':
                    case '\\':
                    case '_':
                    case 'a':
                    case 'b':
                    case 'c':
                    case 'd':
                    case 'f':
                    case 'g':
                    case 'h':
                    case 'i':
                    case 'j':
                    case 'k':
                    case 'l':
                    case 'm':
                    case 'n':
                    case 'o':
                    case 'p':
                    case 'q':
                    case 'r':
                    case 's':
                    case 't':
                    case 'u':
                    case 'v':
                    case 'w':
                    case 'x':
                    case 'y':
                    case 'z':
                        {
                        alt32=112;
                        }
                        break;
                    default:
                        alt32=45;}

                    }
                    break;
                case '$':
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                case 'A':
                case 'B':
                case 'C':
                case 'D':
                case 'E':
                case 'F':
                case 'G':
                case 'H':
                case 'I':
                case 'J':
                case 'K':
                case 'L':
                case 'M':
                case 'N':
                case 'O':
                case 'P':
                case 'Q':
                case 'R':
                case 'S':
                case 'T':
                case 'U':
                case 'V':
                case 'W':
                case 'X':
                case 'Y':
                case 'Z':
                case '\\':
                case '_':
                case 'a':
                case 'b':
                case 'c':
                case 'd':
                case 'e':
                case 'f':
                case 'g':
                case 'h':
                case 'i':
                case 'j':
                case 'k':
                case 'l':
                case 'm':
                case 'n':
                case 'o':
                case 'p':
                case 'q':
                case 'r':
                case 'u':
                case 'v':
                case 'w':
                case 'x':
                case 'y':
                case 'z':
                    {
                    alt32=112;
                    }
                    break;
                default:
                    alt32=16;}

                }
                break;
            case 'f':
                {
                int LA32_71 = input.LA(3);

                if ( (LA32_71=='$'||(LA32_71>='0' && LA32_71<='9')||(LA32_71>='A' && LA32_71<='Z')||LA32_71=='\\'||LA32_71=='_'||(LA32_71>='a' && LA32_71<='z')) ) {
                    alt32=112;
                }
                else {
                    alt32=15;}
                }
                break;
            case 'm':
                {
                int LA32_72 = input.LA(3);

                if ( (LA32_72=='p') ) {
                    switch ( input.LA(4) ) {
                    case 'o':
                        {
                        int LA32_223 = input.LA(5);

                        if ( (LA32_223=='r') ) {
                            int LA32_275 = input.LA(6);

                            if ( (LA32_275=='t') ) {
                                int LA32_318 = input.LA(7);

                                if ( (LA32_318=='$'||(LA32_318>='0' && LA32_318<='9')||(LA32_318>='A' && LA32_318<='Z')||LA32_318=='\\'||LA32_318=='_'||(LA32_318>='a' && LA32_318<='z')) ) {
                                    alt32=112;
                                }
                                else {
                                    alt32=44;}
                            }
                            else {
                                alt32=112;}
                        }
                        else {
                            alt32=112;}
                        }
                        break;
                    case 'l':
                        {
                        int LA32_224 = input.LA(5);

                        if ( (LA32_224=='e') ) {
                            int LA32_276 = input.LA(6);

                            if ( (LA32_276=='m') ) {
                                int LA32_319 = input.LA(7);

                                if ( (LA32_319=='e') ) {
                                    int LA32_350 = input.LA(8);

                                    if ( (LA32_350=='n') ) {
                                        int LA32_371 = input.LA(9);

                                        if ( (LA32_371=='t') ) {
                                            int LA32_384 = input.LA(10);

                                            if ( (LA32_384=='s') ) {
                                                int LA32_392 = input.LA(11);

                                                if ( (LA32_392=='$'||(LA32_392>='0' && LA32_392<='9')||(LA32_392>='A' && LA32_392<='Z')||LA32_392=='\\'||LA32_392=='_'||(LA32_392>='a' && LA32_392<='z')) ) {
                                                    alt32=112;
                                                }
                                                else {
                                                    alt32=43;}
                                            }
                                            else {
                                                alt32=112;}
                                        }
                                        else {
                                            alt32=112;}
                                    }
                                    else {
                                        alt32=112;}
                                }
                                else {
                                    alt32=112;}
                            }
                            else {
                                alt32=112;}
                        }
                        else {
                            alt32=112;}
                        }
                        break;
                    default:
                        alt32=112;}

                }
                else {
                    alt32=112;}
                }
                break;
            default:
                alt32=112;}

            }
            break;
        case 'r':
            {
            int LA32_9 = input.LA(2);

            if ( (LA32_9=='e') ) {
                int LA32_73 = input.LA(3);

                if ( (LA32_73=='t') ) {
                    int LA32_161 = input.LA(4);

                    if ( (LA32_161=='u') ) {
                        int LA32_225 = input.LA(5);

                        if ( (LA32_225=='r') ) {
                            int LA32_277 = input.LA(6);

                            if ( (LA32_277=='n') ) {
                                int LA32_320 = input.LA(7);

                                if ( (LA32_320=='$'||(LA32_320>='0' && LA32_320<='9')||(LA32_320>='A' && LA32_320<='Z')||LA32_320=='\\'||LA32_320=='_'||(LA32_320>='a' && LA32_320<='z')) ) {
                                    alt32=112;
                                }
                                else {
                                    alt32=19;}
                            }
                            else {
                                alt32=112;}
                        }
                        else {
                            alt32=112;}
                    }
                    else {
                        alt32=112;}
                }
                else {
                    alt32=112;}
            }
            else {
                alt32=112;}
            }
            break;
        case 's':
            {
            switch ( input.LA(2) ) {
            case 'y':
                {
                int LA32_74 = input.LA(3);

                if ( (LA32_74=='n') ) {
                    int LA32_162 = input.LA(4);

                    if ( (LA32_162=='c') ) {
                        int LA32_226 = input.LA(5);

                        if ( (LA32_226=='h') ) {
                            int LA32_278 = input.LA(6);

                            if ( (LA32_278=='r') ) {
                                int LA32_321 = input.LA(7);

                                if ( (LA32_321=='o') ) {
                                    int LA32_352 = input.LA(8);

                                    if ( (LA32_352=='n') ) {
                                        int LA32_372 = input.LA(9);

                                        if ( (LA32_372=='i') ) {
                                            int LA32_385 = input.LA(10);

                                            if ( (LA32_385=='z') ) {
                                                int LA32_393 = input.LA(11);

                                                if ( (LA32_393=='e') ) {
                                                    int LA32_397 = input.LA(12);

                                                    if ( (LA32_397=='d') ) {
                                                        int LA32_398 = input.LA(13);

                                                        if ( (LA32_398=='$'||(LA32_398>='0' && LA32_398<='9')||(LA32_398>='A' && LA32_398<='Z')||LA32_398=='\\'||LA32_398=='_'||(LA32_398>='a' && LA32_398<='z')) ) {
                                                            alt32=112;
                                                        }
                                                        else {
                                                            alt32=56;}
                                                    }
                                                    else {
                                                        alt32=112;}
                                                }
                                                else {
                                                    alt32=112;}
                                            }
                                            else {
                                                alt32=112;}
                                        }
                                        else {
                                            alt32=112;}
                                    }
                                    else {
                                        alt32=112;}
                                }
                                else {
                                    alt32=112;}
                            }
                            else {
                                alt32=112;}
                        }
                        else {
                            alt32=112;}
                    }
                    else {
                        alt32=112;}
                }
                else {
                    alt32=112;}
                }
                break;
            case 'u':
                {
                int LA32_75 = input.LA(3);

                if ( (LA32_75=='p') ) {
                    int LA32_163 = input.LA(4);

                    if ( (LA32_163=='e') ) {
                        int LA32_227 = input.LA(5);

                        if ( (LA32_227=='r') ) {
                            int LA32_279 = input.LA(6);

                            if ( (LA32_279=='$'||(LA32_279>='0' && LA32_279<='9')||(LA32_279>='A' && LA32_279<='Z')||LA32_279=='\\'||LA32_279=='_'||(LA32_279>='a' && LA32_279<='z')) ) {
                                alt32=112;
                            }
                            else {
                                alt32=55;}
                        }
                        else {
                            alt32=112;}
                    }
                    else {
                        alt32=112;}
                }
                else {
                    alt32=112;}
                }
                break;
            case 'h':
                {
                int LA32_76 = input.LA(3);

                if ( (LA32_76=='o') ) {
                    int LA32_164 = input.LA(4);

                    if ( (LA32_164=='r') ) {
                        int LA32_228 = input.LA(5);

                        if ( (LA32_228=='t') ) {
                            int LA32_280 = input.LA(6);

                            if ( (LA32_280=='$'||(LA32_280>='0' && LA32_280<='9')||(LA32_280>='A' && LA32_280<='Z')||LA32_280=='\\'||LA32_280=='_'||(LA32_280>='a' && LA32_280<='z')) ) {
                                alt32=112;
                            }
                            else {
                                alt32=53;}
                        }
                        else {
                            alt32=112;}
                    }
                    else {
                        alt32=112;}
                }
                else {
                    alt32=112;}
                }
                break;
            case 't':
                {
                int LA32_77 = input.LA(3);

                if ( (LA32_77=='a') ) {
                    int LA32_165 = input.LA(4);

                    if ( (LA32_165=='t') ) {
                        int LA32_229 = input.LA(5);

                        if ( (LA32_229=='i') ) {
                            int LA32_281 = input.LA(6);

                            if ( (LA32_281=='c') ) {
                                int LA32_324 = input.LA(7);

                                if ( (LA32_324=='$'||(LA32_324>='0' && LA32_324<='9')||(LA32_324>='A' && LA32_324<='Z')||LA32_324=='\\'||LA32_324=='_'||(LA32_324>='a' && LA32_324<='z')) ) {
                                    alt32=112;
                                }
                                else {
                                    alt32=54;}
                            }
                            else {
                                alt32=112;}
                        }
                        else {
                            alt32=112;}
                    }
                    else {
                        alt32=112;}
                }
                else {
                    alt32=112;}
                }
                break;
            case 'w':
                {
                int LA32_78 = input.LA(3);

                if ( (LA32_78=='i') ) {
                    int LA32_166 = input.LA(4);

                    if ( (LA32_166=='t') ) {
                        int LA32_230 = input.LA(5);

                        if ( (LA32_230=='c') ) {
                            int LA32_282 = input.LA(6);

                            if ( (LA32_282=='h') ) {
                                int LA32_325 = input.LA(7);

                                if ( (LA32_325=='$'||(LA32_325>='0' && LA32_325<='9')||(LA32_325>='A' && LA32_325<='Z')||LA32_325=='\\'||LA32_325=='_'||(LA32_325>='a' && LA32_325<='z')) ) {
                                    alt32=112;
                                }
                                else {
                                    alt32=20;}
                            }
                            else {
                                alt32=112;}
                        }
                        else {
                            alt32=112;}
                    }
                    else {
                        alt32=112;}
                }
                else {
                    alt32=112;}
                }
                break;
            default:
                alt32=112;}

            }
            break;
        case 'v':
            {
            switch ( input.LA(2) ) {
            case 'o':
                {
                switch ( input.LA(3) ) {
                case 'i':
                    {
                    int LA32_167 = input.LA(4);

                    if ( (LA32_167=='d') ) {
                        int LA32_231 = input.LA(5);

                        if ( (LA32_231=='$'||(LA32_231>='0' && LA32_231<='9')||(LA32_231>='A' && LA32_231<='Z')||LA32_231=='\\'||LA32_231=='_'||(LA32_231>='a' && LA32_231<='z')) ) {
                            alt32=112;
                        }
                        else {
                            alt32=26;}
                    }
                    else {
                        alt32=112;}
                    }
                    break;
                case 'l':
                    {
                    int LA32_168 = input.LA(4);

                    if ( (LA32_168=='a') ) {
                        int LA32_232 = input.LA(5);

                        if ( (LA32_232=='t') ) {
                            int LA32_284 = input.LA(6);

                            if ( (LA32_284=='i') ) {
                                int LA32_326 = input.LA(7);

                                if ( (LA32_326=='l') ) {
                                    int LA32_355 = input.LA(8);

                                    if ( (LA32_355=='e') ) {
                                        int LA32_373 = input.LA(9);

                                        if ( (LA32_373=='$'||(LA32_373>='0' && LA32_373<='9')||(LA32_373>='A' && LA32_373<='Z')||LA32_373=='\\'||LA32_373=='_'||(LA32_373>='a' && LA32_373<='z')) ) {
                                            alt32=112;
                                        }
                                        else {
                                            alt32=59;}
                                    }
                                    else {
                                        alt32=112;}
                                }
                                else {
                                    alt32=112;}
                            }
                            else {
                                alt32=112;}
                        }
                        else {
                            alt32=112;}
                    }
                    else {
                        alt32=112;}
                    }
                    break;
                default:
                    alt32=112;}

                }
                break;
            case 'a':
                {
                int LA32_80 = input.LA(3);

                if ( (LA32_80=='r') ) {
                    int LA32_169 = input.LA(4);

                    if ( (LA32_169=='$'||(LA32_169>='0' && LA32_169<='9')||(LA32_169>='A' && LA32_169<='Z')||LA32_169=='\\'||LA32_169=='_'||(LA32_169>='a' && LA32_169<='z')) ) {
                        alt32=112;
                    }
                    else {
                        alt32=25;}
                }
                else {
                    alt32=112;}
                }
                break;
            default:
                alt32=112;}

            }
            break;
        case 'w':
            {
            switch ( input.LA(2) ) {
            case 'i':
                {
                int LA32_81 = input.LA(3);

                if ( (LA32_81=='t') ) {
                    int LA32_170 = input.LA(4);

                    if ( (LA32_170=='h') ) {
                        int LA32_234 = input.LA(5);

                        if ( (LA32_234=='$'||(LA32_234>='0' && LA32_234<='9')||(LA32_234>='A' && LA32_234<='Z')||LA32_234=='\\'||LA32_234=='_'||(LA32_234>='a' && LA32_234<='z')) ) {
                            alt32=112;
                        }
                        else {
                            alt32=28;}
                    }
                    else {
                        alt32=112;}
                }
                else {
                    alt32=112;}
                }
                break;
            case 'h':
                {
                int LA32_82 = input.LA(3);

                if ( (LA32_82=='i') ) {
                    int LA32_171 = input.LA(4);

                    if ( (LA32_171=='l') ) {
                        int LA32_235 = input.LA(5);

                        if ( (LA32_235=='e') ) {
                            int LA32_286 = input.LA(6);

                            if ( (LA32_286=='$'||(LA32_286>='0' && LA32_286<='9')||(LA32_286>='A' && LA32_286<='Z')||LA32_286=='\\'||LA32_286=='_'||(LA32_286>='a' && LA32_286<='z')) ) {
                                alt32=112;
                            }
                            else {
                                alt32=27;}
                        }
                        else {
                            alt32=112;}
                    }
                    else {
                        alt32=112;}
                }
                else {
                    alt32=112;}
                }
                break;
            default:
                alt32=112;}

            }
            break;
        case 'a':
            {
            int LA32_13 = input.LA(2);

            if ( (LA32_13=='b') ) {
                int LA32_83 = input.LA(3);

                if ( (LA32_83=='s') ) {
                    int LA32_172 = input.LA(4);

                    if ( (LA32_172=='t') ) {
                        int LA32_236 = input.LA(5);

                        if ( (LA32_236=='r') ) {
                            int LA32_287 = input.LA(6);

                            if ( (LA32_287=='a') ) {
                                int LA32_328 = input.LA(7);

                                if ( (LA32_328=='c') ) {
                                    int LA32_356 = input.LA(8);

                                    if ( (LA32_356=='t') ) {
                                        int LA32_374 = input.LA(9);

                                        if ( (LA32_374=='$'||(LA32_374>='0' && LA32_374<='9')||(LA32_374>='A' && LA32_374<='Z')||LA32_374=='\\'||LA32_374=='_'||(LA32_374>='a' && LA32_374<='z')) ) {
                                            alt32=112;
                                        }
                                        else {
                                            alt32=29;}
                                    }
                                    else {
                                        alt32=112;}
                                }
                                else {
                                    alt32=112;}
                            }
                            else {
                                alt32=112;}
                        }
                        else {
                            alt32=112;}
                    }
                    else {
                        alt32=112;}
                }
                else {
                    alt32=112;}
            }
            else {
                alt32=112;}
            }
            break;
        case 'g':
            {
            int LA32_14 = input.LA(2);

            if ( (LA32_14=='o') ) {
                int LA32_84 = input.LA(3);

                if ( (LA32_84=='t') ) {
                    int LA32_173 = input.LA(4);

                    if ( (LA32_173=='o') ) {
                        int LA32_237 = input.LA(5);

                        if ( (LA32_237=='$'||(LA32_237>='0' && LA32_237<='9')||(LA32_237>='A' && LA32_237<='Z')||LA32_237=='\\'||LA32_237=='_'||(LA32_237>='a' && LA32_237<='z')) ) {
                            alt32=112;
                        }
                        else {
                            alt32=42;}
                    }
                    else {
                        alt32=112;}
                }
                else {
                    alt32=112;}
            }
            else {
                alt32=112;}
            }
            break;
        case 'l':
            {
            int LA32_15 = input.LA(2);

            if ( (LA32_15=='o') ) {
                int LA32_85 = input.LA(3);

                if ( (LA32_85=='n') ) {
                    int LA32_174 = input.LA(4);

                    if ( (LA32_174=='g') ) {
                        int LA32_238 = input.LA(5);

                        if ( (LA32_238=='$'||(LA32_238>='0' && LA32_238<='9')||(LA32_238>='A' && LA32_238<='Z')||LA32_238=='\\'||LA32_238=='_'||(LA32_238>='a' && LA32_238<='z')) ) {
                            alt32=112;
                        }
                        else {
                            alt32=47;}
                    }
                    else {
                        alt32=112;}
                }
                else {
                    alt32=112;}
            }
            else {
                alt32=112;}
            }
            break;
        case 'p':
            {
            switch ( input.LA(2) ) {
            case 'a':
                {
                int LA32_86 = input.LA(3);

                if ( (LA32_86=='c') ) {
                    int LA32_175 = input.LA(4);

                    if ( (LA32_175=='k') ) {
                        int LA32_239 = input.LA(5);

                        if ( (LA32_239=='a') ) {
                            int LA32_290 = input.LA(6);

                            if ( (LA32_290=='g') ) {
                                int LA32_329 = input.LA(7);

                                if ( (LA32_329=='e') ) {
                                    int LA32_357 = input.LA(8);

                                    if ( (LA32_357=='$'||(LA32_357>='0' && LA32_357<='9')||(LA32_357>='A' && LA32_357<='Z')||LA32_357=='\\'||LA32_357=='_'||(LA32_357>='a' && LA32_357<='z')) ) {
                                        alt32=112;
                                    }
                                    else {
                                        alt32=49;}
                                }
                                else {
                                    alt32=112;}
                            }
                            else {
                                alt32=112;}
                        }
                        else {
                            alt32=112;}
                    }
                    else {
                        alt32=112;}
                }
                else {
                    alt32=112;}
                }
                break;
            case 'r':
                {
                switch ( input.LA(3) ) {
                case 'o':
                    {
                    int LA32_176 = input.LA(4);

                    if ( (LA32_176=='t') ) {
                        int LA32_240 = input.LA(5);

                        if ( (LA32_240=='e') ) {
                            int LA32_291 = input.LA(6);

                            if ( (LA32_291=='c') ) {
                                int LA32_330 = input.LA(7);

                                if ( (LA32_330=='t') ) {
                                    int LA32_358 = input.LA(8);

                                    if ( (LA32_358=='e') ) {
                                        int LA32_376 = input.LA(9);

                                        if ( (LA32_376=='d') ) {
                                            int LA32_388 = input.LA(10);

                                            if ( (LA32_388=='$'||(LA32_388>='0' && LA32_388<='9')||(LA32_388>='A' && LA32_388<='Z')||LA32_388=='\\'||LA32_388=='_'||(LA32_388>='a' && LA32_388<='z')) ) {
                                                alt32=112;
                                            }
                                            else {
                                                alt32=51;}
                                        }
                                        else {
                                            alt32=112;}
                                    }
                                    else {
                                        alt32=112;}
                                }
                                else {
                                    alt32=112;}
                            }
                            else {
                                alt32=112;}
                        }
                        else {
                            alt32=112;}
                    }
                    else {
                        alt32=112;}
                    }
                    break;
                case 'i':
                    {
                    int LA32_177 = input.LA(4);

                    if ( (LA32_177=='v') ) {
                        int LA32_241 = input.LA(5);

                        if ( (LA32_241=='a') ) {
                            int LA32_292 = input.LA(6);

                            if ( (LA32_292=='t') ) {
                                int LA32_331 = input.LA(7);

                                if ( (LA32_331=='e') ) {
                                    int LA32_359 = input.LA(8);

                                    if ( (LA32_359=='$'||(LA32_359>='0' && LA32_359<='9')||(LA32_359>='A' && LA32_359<='Z')||LA32_359=='\\'||LA32_359=='_'||(LA32_359>='a' && LA32_359<='z')) ) {
                                        alt32=112;
                                    }
                                    else {
                                        alt32=50;}
                                }
                                else {
                                    alt32=112;}
                            }
                            else {
                                alt32=112;}
                        }
                        else {
                            alt32=112;}
                    }
                    else {
                        alt32=112;}
                    }
                    break;
                default:
                    alt32=112;}

                }
                break;
            case 'u':
                {
                int LA32_88 = input.LA(3);

                if ( (LA32_88=='b') ) {
                    int LA32_178 = input.LA(4);

                    if ( (LA32_178=='l') ) {
                        int LA32_242 = input.LA(5);

                        if ( (LA32_242=='i') ) {
                            int LA32_293 = input.LA(6);

                            if ( (LA32_293=='c') ) {
                                int LA32_332 = input.LA(7);

                                if ( (LA32_332=='$'||(LA32_332>='0' && LA32_332<='9')||(LA32_332>='A' && LA32_332<='Z')||LA32_332=='\\'||LA32_332=='_'||(LA32_332>='a' && LA32_332<='z')) ) {
                                    alt32=112;
                                }
                                else {
                                    alt32=52;}
                            }
                            else {
                                alt32=112;}
                        }
                        else {
                            alt32=112;}
                    }
                    else {
                        alt32=112;}
                }
                else {
                    alt32=112;}
                }
                break;
            default:
                alt32=112;}

            }
            break;
        case '{':
            {
            alt32=60;
            }
            break;
        case '}':
            {
            alt32=61;
            }
            break;
        case '(':
            {
            alt32=62;
            }
            break;
        case ')':
            {
            alt32=63;
            }
            break;
        case '[':
            {
            alt32=64;
            }
            break;
        case ']':
            {
            alt32=65;
            }
            break;
        case '.':
            {
            int LA32_23 = input.LA(2);

            if ( ((LA32_23>='0' && LA32_23<='9')) ) {
                alt32=113;
            }
            else {
                alt32=66;}
            }
            break;
        case ';':
            {
            alt32=67;
            }
            break;
        case ',':
            {
            alt32=68;
            }
            break;
        case '<':
            {
            switch ( input.LA(2) ) {
            case '=':
                {
                alt32=71;
                }
                break;
            case '<':
                {
                int LA32_91 = input.LA(3);

                if ( (LA32_91=='=') ) {
                    alt32=100;
                }
                else {
                    alt32=83;}
                }
                break;
            default:
                alt32=69;}

            }
            break;
        case '>':
            {
            switch ( input.LA(2) ) {
            case '>':
                {
                switch ( input.LA(3) ) {
                case '>':
                    {
                    int LA32_181 = input.LA(4);

                    if ( (LA32_181=='=') ) {
                        alt32=102;
                    }
                    else {
                        alt32=85;}
                    }
                    break;
                case '=':
                    {
                    alt32=101;
                    }
                    break;
                default:
                    alt32=84;}

                }
                break;
            case '=':
                {
                alt32=72;
                }
                break;
            default:
                alt32=70;}

            }
            break;
        case '=':
            {
            int LA32_28 = input.LA(2);

            if ( (LA32_28=='=') ) {
                int LA32_96 = input.LA(3);

                if ( (LA32_96=='=') ) {
                    alt32=75;
                }
                else {
                    alt32=73;}
            }
            else {
                alt32=95;}
            }
            break;
        case '!':
            {
            int LA32_29 = input.LA(2);

            if ( (LA32_29=='=') ) {
                int LA32_98 = input.LA(3);

                if ( (LA32_98=='=') ) {
                    alt32=76;
                }
                else {
                    alt32=74;}
            }
            else {
                alt32=89;}
            }
            break;
        case '+':
            {
            switch ( input.LA(2) ) {
            case '=':
                {
                alt32=96;
                }
                break;
            case '+':
                {
                alt32=81;
                }
                break;
            default:
                alt32=77;}

            }
            break;
        case '-':
            {
            switch ( input.LA(2) ) {
            case '=':
                {
                alt32=97;
                }
                break;
            case '-':
                {
                alt32=82;
                }
                break;
            default:
                alt32=78;}

            }
            break;
        case '*':
            {
            int LA32_32 = input.LA(2);

            if ( (LA32_32=='=') ) {
                alt32=98;
            }
            else {
                alt32=79;}
            }
            break;
        case '%':
            {
            int LA32_33 = input.LA(2);

            if ( (LA32_33=='=') ) {
                alt32=99;
            }
            else {
                alt32=80;}
            }
            break;
        case '&':
            {
            switch ( input.LA(2) ) {
            case '&':
                {
                alt32=91;
                }
                break;
            case '=':
                {
                alt32=103;
                }
                break;
            default:
                alt32=86;}

            }
            break;
        case '|':
            {
            switch ( input.LA(2) ) {
            case '|':
                {
                alt32=92;
                }
                break;
            case '=':
                {
                alt32=104;
                }
                break;
            default:
                alt32=87;}

            }
            break;
        case '^':
            {
            int LA32_36 = input.LA(2);

            if ( (LA32_36=='=') ) {
                alt32=105;
            }
            else {
                alt32=88;}
            }
            break;
        case '~':
            {
            alt32=90;
            }
            break;
        case '?':
            {
            alt32=93;
            }
            break;
        case ':':
            {
            alt32=94;
            }
            break;
        case '/':
            {
            int LA32_40 = input.LA(2);

            if ( (LA32_40=='=') ) {
                int LA32_118 = input.LA(3);

                if ( ((LA32_118>='\u0000' && LA32_118<='\t')||(LA32_118>='\u000B' && LA32_118<='\f')||(LA32_118>='\u000E' && LA32_118<='\u2027')||(LA32_118>='\u202A' && LA32_118<='\uFFFE')) && ( areRegularExpressionsEnabled() )) {
                    alt32=117;
                }
                else {
                    alt32=107;}
            }
            else if ( (LA32_40=='/') ) {
                alt32=111;
            }
            else if ( (LA32_40=='*') ) {
                alt32=110;
            }
            else if ( ((LA32_40>='\u0000' && LA32_40<='\t')||(LA32_40>='\u000B' && LA32_40<='\f')||(LA32_40>='\u000E' && LA32_40<=')')||(LA32_40>='+' && LA32_40<='.')||(LA32_40>='0' && LA32_40<='<')||(LA32_40>='>' && LA32_40<='\u2027')||(LA32_40>='\u202A' && LA32_40<='\uFFFE')) && ( areRegularExpressionsEnabled() )) {
                alt32=117;
            }
            else {
                alt32=106;}
            }
            break;
        case '\t':
        case '\u000B':
        case '\f':
        case ' ':
        case '\u00A0':
        case '\u1680':
        case '\u180E':
        case '\u2000':
        case '\u2001':
        case '\u2002':
        case '\u2003':
        case '\u2004':
        case '\u2005':
        case '\u2006':
        case '\u2007':
        case '\u2008':
        case '\u2009':
        case '\u200A':
        case '\u202F':
        case '\u205F':
        case '\u3000':
            {
            alt32=108;
            }
            break;
        case '\n':
        case '\r':
        case '\u2028':
        case '\u2029':
            {
            alt32=109;
            }
            break;
        case '0':
            {
            switch ( input.LA(2) ) {
            case 'X':
            case 'x':
                {
                alt32=115;
                }
                break;
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
                {
                alt32=114;
                }
                break;
            default:
                alt32=113;}

            }
            break;
        case '1':
        case '2':
        case '3':
        case '4':
        case '5':
        case '6':
        case '7':
        case '8':
        case '9':
            {
            alt32=113;
            }
            break;
        case '\"':
        case '\'':
            {
            alt32=116;
            }
            break;
        default:
            alt32=112;}

        switch (alt32) {
            case 1 :
                // ES3.g3:1:10: NULL
                {
                mNULL(); 

                }
                break;
            case 2 :
                // ES3.g3:1:15: TRUE
                {
                mTRUE(); 

                }
                break;
            case 3 :
                // ES3.g3:1:20: FALSE
                {
                mFALSE(); 

                }
                break;
            case 4 :
                // ES3.g3:1:26: BREAK
                {
                mBREAK(); 

                }
                break;
            case 5 :
                // ES3.g3:1:32: CASE
                {
                mCASE(); 

                }
                break;
            case 6 :
                // ES3.g3:1:37: CATCH
                {
                mCATCH(); 

                }
                break;
            case 7 :
                // ES3.g3:1:43: CONTINUE
                {
                mCONTINUE(); 

                }
                break;
            case 8 :
                // ES3.g3:1:52: DEFAULT
                {
                mDEFAULT(); 

                }
                break;
            case 9 :
                // ES3.g3:1:60: DELETE
                {
                mDELETE(); 

                }
                break;
            case 10 :
                // ES3.g3:1:67: DO
                {
                mDO(); 

                }
                break;
            case 11 :
                // ES3.g3:1:70: ELSE
                {
                mELSE(); 

                }
                break;
            case 12 :
                // ES3.g3:1:75: FINALLY
                {
                mFINALLY(); 

                }
                break;
            case 13 :
                // ES3.g3:1:83: FOR
                {
                mFOR(); 

                }
                break;
            case 14 :
                // ES3.g3:1:87: FUNCTION
                {
                mFUNCTION(); 

                }
                break;
            case 15 :
                // ES3.g3:1:96: IF
                {
                mIF(); 

                }
                break;
            case 16 :
                // ES3.g3:1:99: IN
                {
                mIN(); 

                }
                break;
            case 17 :
                // ES3.g3:1:102: INSTANCEOF
                {
                mINSTANCEOF(); 

                }
                break;
            case 18 :
                // ES3.g3:1:113: NEW
                {
                mNEW(); 

                }
                break;
            case 19 :
                // ES3.g3:1:117: RETURN
                {
                mRETURN(); 

                }
                break;
            case 20 :
                // ES3.g3:1:124: SWITCH
                {
                mSWITCH(); 

                }
                break;
            case 21 :
                // ES3.g3:1:131: THIS
                {
                mTHIS(); 

                }
                break;
            case 22 :
                // ES3.g3:1:136: THROW
                {
                mTHROW(); 

                }
                break;
            case 23 :
                // ES3.g3:1:142: TRY
                {
                mTRY(); 

                }
                break;
            case 24 :
                // ES3.g3:1:146: TYPEOF
                {
                mTYPEOF(); 

                }
                break;
            case 25 :
                // ES3.g3:1:153: VAR
                {
                mVAR(); 

                }
                break;
            case 26 :
                // ES3.g3:1:157: VOID
                {
                mVOID(); 

                }
                break;
            case 27 :
                // ES3.g3:1:162: WHILE
                {
                mWHILE(); 

                }
                break;
            case 28 :
                // ES3.g3:1:168: WITH
                {
                mWITH(); 

                }
                break;
            case 29 :
                // ES3.g3:1:173: ABSTRACT
                {
                mABSTRACT(); 

                }
                break;
            case 30 :
                // ES3.g3:1:182: BOOLEAN
                {
                mBOOLEAN(); 

                }
                break;
            case 31 :
                // ES3.g3:1:190: BYTE
                {
                mBYTE(); 

                }
                break;
            case 32 :
                // ES3.g3:1:195: CHAR
                {
                mCHAR(); 

                }
                break;
            case 33 :
                // ES3.g3:1:200: CLASS
                {
                mCLASS(); 

                }
                break;
            case 34 :
                // ES3.g3:1:206: CONST
                {
                mCONST(); 

                }
                break;
            case 35 :
                // ES3.g3:1:212: DEBUGGER
                {
                mDEBUGGER(); 

                }
                break;
            case 36 :
                // ES3.g3:1:221: DOUBLE
                {
                mDOUBLE(); 

                }
                break;
            case 37 :
                // ES3.g3:1:228: ENUM
                {
                mENUM(); 

                }
                break;
            case 38 :
                // ES3.g3:1:233: EXPORT
                {
                mEXPORT(); 

                }
                break;
            case 39 :
                // ES3.g3:1:240: EXTENDS
                {
                mEXTENDS(); 

                }
                break;
            case 40 :
                // ES3.g3:1:248: FINAL
                {
                mFINAL(); 

                }
                break;
            case 41 :
                // ES3.g3:1:254: FLOAT
                {
                mFLOAT(); 

                }
                break;
            case 42 :
                // ES3.g3:1:260: GOTO
                {
                mGOTO(); 

                }
                break;
            case 43 :
                // ES3.g3:1:265: IMPLEMENTS
                {
                mIMPLEMENTS(); 

                }
                break;
            case 44 :
                // ES3.g3:1:276: IMPORT
                {
                mIMPORT(); 

                }
                break;
            case 45 :
                // ES3.g3:1:283: INT
                {
                mINT(); 

                }
                break;
            case 46 :
                // ES3.g3:1:287: INTERFACE
                {
                mINTERFACE(); 

                }
                break;
            case 47 :
                // ES3.g3:1:297: LONG
                {
                mLONG(); 

                }
                break;
            case 48 :
                // ES3.g3:1:302: NATIVE
                {
                mNATIVE(); 

                }
                break;
            case 49 :
                // ES3.g3:1:309: PACKAGE
                {
                mPACKAGE(); 

                }
                break;
            case 50 :
                // ES3.g3:1:317: PRIVATE
                {
                mPRIVATE(); 

                }
                break;
            case 51 :
                // ES3.g3:1:325: PROTECTED
                {
                mPROTECTED(); 

                }
                break;
            case 52 :
                // ES3.g3:1:335: PUBLIC
                {
                mPUBLIC(); 

                }
                break;
            case 53 :
                // ES3.g3:1:342: SHORT
                {
                mSHORT(); 

                }
                break;
            case 54 :
                // ES3.g3:1:348: STATIC
                {
                mSTATIC(); 

                }
                break;
            case 55 :
                // ES3.g3:1:355: SUPER
                {
                mSUPER(); 

                }
                break;
            case 56 :
                // ES3.g3:1:361: SYNCHRONIZED
                {
                mSYNCHRONIZED(); 

                }
                break;
            case 57 :
                // ES3.g3:1:374: THROWS
                {
                mTHROWS(); 

                }
                break;
            case 58 :
                // ES3.g3:1:381: TRANSIENT
                {
                mTRANSIENT(); 

                }
                break;
            case 59 :
                // ES3.g3:1:391: VOLATILE
                {
                mVOLATILE(); 

                }
                break;
            case 60 :
                // ES3.g3:1:400: LBRACE
                {
                mLBRACE(); 

                }
                break;
            case 61 :
                // ES3.g3:1:407: RBRACE
                {
                mRBRACE(); 

                }
                break;
            case 62 :
                // ES3.g3:1:414: LPAREN
                {
                mLPAREN(); 

                }
                break;
            case 63 :
                // ES3.g3:1:421: RPAREN
                {
                mRPAREN(); 

                }
                break;
            case 64 :
                // ES3.g3:1:428: LBRACK
                {
                mLBRACK(); 

                }
                break;
            case 65 :
                // ES3.g3:1:435: RBRACK
                {
                mRBRACK(); 

                }
                break;
            case 66 :
                // ES3.g3:1:442: DOT
                {
                mDOT(); 

                }
                break;
            case 67 :
                // ES3.g3:1:446: SEMIC
                {
                mSEMIC(); 

                }
                break;
            case 68 :
                // ES3.g3:1:452: COMMA
                {
                mCOMMA(); 

                }
                break;
            case 69 :
                // ES3.g3:1:458: LT
                {
                mLT(); 

                }
                break;
            case 70 :
                // ES3.g3:1:461: GT
                {
                mGT(); 

                }
                break;
            case 71 :
                // ES3.g3:1:464: LTE
                {
                mLTE(); 

                }
                break;
            case 72 :
                // ES3.g3:1:468: GTE
                {
                mGTE(); 

                }
                break;
            case 73 :
                // ES3.g3:1:472: EQ
                {
                mEQ(); 

                }
                break;
            case 74 :
                // ES3.g3:1:475: NEQ
                {
                mNEQ(); 

                }
                break;
            case 75 :
                // ES3.g3:1:479: SAME
                {
                mSAME(); 

                }
                break;
            case 76 :
                // ES3.g3:1:484: NSAME
                {
                mNSAME(); 

                }
                break;
            case 77 :
                // ES3.g3:1:490: ADD
                {
                mADD(); 

                }
                break;
            case 78 :
                // ES3.g3:1:494: SUB
                {
                mSUB(); 

                }
                break;
            case 79 :
                // ES3.g3:1:498: MUL
                {
                mMUL(); 

                }
                break;
            case 80 :
                // ES3.g3:1:502: MOD
                {
                mMOD(); 

                }
                break;
            case 81 :
                // ES3.g3:1:506: INC
                {
                mINC(); 

                }
                break;
            case 82 :
                // ES3.g3:1:510: DEC
                {
                mDEC(); 

                }
                break;
            case 83 :
                // ES3.g3:1:514: SHL
                {
                mSHL(); 

                }
                break;
            case 84 :
                // ES3.g3:1:518: SHR
                {
                mSHR(); 

                }
                break;
            case 85 :
                // ES3.g3:1:522: SHU
                {
                mSHU(); 

                }
                break;
            case 86 :
                // ES3.g3:1:526: AND
                {
                mAND(); 

                }
                break;
            case 87 :
                // ES3.g3:1:530: OR
                {
                mOR(); 

                }
                break;
            case 88 :
                // ES3.g3:1:533: XOR
                {
                mXOR(); 

                }
                break;
            case 89 :
                // ES3.g3:1:537: NOT
                {
                mNOT(); 

                }
                break;
            case 90 :
                // ES3.g3:1:541: INV
                {
                mINV(); 

                }
                break;
            case 91 :
                // ES3.g3:1:545: LAND
                {
                mLAND(); 

                }
                break;
            case 92 :
                // ES3.g3:1:550: LOR
                {
                mLOR(); 

                }
                break;
            case 93 :
                // ES3.g3:1:554: QUE
                {
                mQUE(); 

                }
                break;
            case 94 :
                // ES3.g3:1:558: COLON
                {
                mCOLON(); 

                }
                break;
            case 95 :
                // ES3.g3:1:564: ASSIGN
                {
                mASSIGN(); 

                }
                break;
            case 96 :
                // ES3.g3:1:571: ADDASS
                {
                mADDASS(); 

                }
                break;
            case 97 :
                // ES3.g3:1:578: SUBASS
                {
                mSUBASS(); 

                }
                break;
            case 98 :
                // ES3.g3:1:585: MULASS
                {
                mMULASS(); 

                }
                break;
            case 99 :
                // ES3.g3:1:592: MODASS
                {
                mMODASS(); 

                }
                break;
            case 100 :
                // ES3.g3:1:599: SHLASS
                {
                mSHLASS(); 

                }
                break;
            case 101 :
                // ES3.g3:1:606: SHRASS
                {
                mSHRASS(); 

                }
                break;
            case 102 :
                // ES3.g3:1:613: SHUASS
                {
                mSHUASS(); 

                }
                break;
            case 103 :
                // ES3.g3:1:620: ANDASS
                {
                mANDASS(); 

                }
                break;
            case 104 :
                // ES3.g3:1:627: ORASS
                {
                mORASS(); 

                }
                break;
            case 105 :
                // ES3.g3:1:633: XORASS
                {
                mXORASS(); 

                }
                break;
            case 106 :
                // ES3.g3:1:640: DIV
                {
                mDIV(); 

                }
                break;
            case 107 :
                // ES3.g3:1:644: DIVASS
                {
                mDIVASS(); 

                }
                break;
            case 108 :
                // ES3.g3:1:651: WhiteSpace
                {
                mWhiteSpace(); 

                }
                break;
            case 109 :
                // ES3.g3:1:662: EOL
                {
                mEOL(); 

                }
                break;
            case 110 :
                // ES3.g3:1:666: MultiLineComment
                {
                mMultiLineComment(); 

                }
                break;
            case 111 :
                // ES3.g3:1:683: SingleLineComment
                {
                mSingleLineComment(); 

                }
                break;
            case 112 :
                // ES3.g3:1:701: Identifier
                {
                mIdentifier(); 

                }
                break;
            case 113 :
                // ES3.g3:1:712: DecimalLiteral
                {
                mDecimalLiteral(); 

                }
                break;
            case 114 :
                // ES3.g3:1:727: OctalIntegerLiteral
                {
                mOctalIntegerLiteral(); 

                }
                break;
            case 115 :
                // ES3.g3:1:747: HexIntegerLiteral
                {
                mHexIntegerLiteral(); 

                }
                break;
            case 116 :
                // ES3.g3:1:765: StringLiteral
                {
                mStringLiteral(); 

                }
                break;
            case 117 :
                // ES3.g3:1:779: RegularExpressionLiteral
                {
                mRegularExpressionLiteral(); 

                }
                break;

        }

    }


    protected DFA19 dfa19 = new DFA19(this);
    static final String DFA19_eotS =
        "\1\uffff\2\4\3\uffff\1\4";
    static final String DFA19_eofS =
        "\7\uffff";
    static final String DFA19_minS =
        "\3\56\3\uffff\1\56";
    static final String DFA19_maxS =
        "\1\71\1\56\1\71\3\uffff\1\71";
    static final String DFA19_acceptS =
        "\3\uffff\1\2\1\3\1\1\1\uffff";
    static final String DFA19_specialS =
        "\7\uffff}>";
    static final String[] DFA19_transitionS = {
            "\1\3\1\uffff\1\1\11\2",
            "\1\5",
            "\1\5\1\uffff\12\6",
            "",
            "",
            "",
            "\1\5\1\uffff\12\6"
    };

    static final short[] DFA19_eot = DFA.unpackEncodedString(DFA19_eotS);
    static final short[] DFA19_eof = DFA.unpackEncodedString(DFA19_eofS);
    static final char[] DFA19_min = DFA.unpackEncodedStringToUnsignedChars(DFA19_minS);
    static final char[] DFA19_max = DFA.unpackEncodedStringToUnsignedChars(DFA19_maxS);
    static final short[] DFA19_accept = DFA.unpackEncodedString(DFA19_acceptS);
    static final short[] DFA19_special = DFA.unpackEncodedString(DFA19_specialS);
    static final short[][] DFA19_transition;

    static {
        int numStates = DFA19_transitionS.length;
        DFA19_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA19_transition[i] = DFA.unpackEncodedString(DFA19_transitionS[i]);
        }
    }

    class DFA19 extends DFA {

        public DFA19(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 19;
            this.eot = DFA19_eot;
            this.eof = DFA19_eof;
            this.min = DFA19_min;
            this.max = DFA19_max;
            this.accept = DFA19_accept;
            this.special = DFA19_special;
            this.transition = DFA19_transition;
        }
        public String getDescription() {
            return "740:1: DecimalLiteral : ( DecimalIntegerLiteral '.' ( DecimalDigit )* ( ExponentPart )? | '.' ( DecimalDigit )+ ( ExponentPart )? | DecimalIntegerLiteral ( ExponentPart )? );";
        }
    }
 

}