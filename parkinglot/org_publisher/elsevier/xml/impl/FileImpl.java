//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v1.0.5-b16-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2006.02.13 at 02:58:03 PM EST 
//


package org.publisher.elsevier.xml.impl;

public class FileImpl implements org.publisher.elsevier.xml.File, com.sun.xml.bind.RIElement, com.sun.xml.bind.JAXBObject, org.publisher.elsevier.xml.impl.runtime.UnmarshallableObject, org.publisher.elsevier.xml.impl.runtime.XMLSerializable, org.publisher.elsevier.xml.impl.runtime.ValidatableObject
{

    protected java.lang.String _Size;
    protected java.lang.String _Md5;
    protected java.lang.String _Name;
    public final static java.lang.Class version = (org.publisher.elsevier.xml.impl.JAXBVersion.class);
    private static com.sun.msv.grammar.Grammar schemaFragment;

    private final static java.lang.Class PRIMARY_INTERFACE_CLASS() {
        return (org.publisher.elsevier.xml.File.class);
    }

    public java.lang.String ____jaxb_ri____getNamespaceURI() {
        return "";
    }

    public java.lang.String ____jaxb_ri____getLocalName() {
        return "file";
    }

    public java.lang.String getSize() {
        return _Size;
    }

    public void setSize(java.lang.String value) {
        _Size = value;
    }

    public java.lang.String getMd5() {
        return _Md5;
    }

    public void setMd5(java.lang.String value) {
        _Md5 = value;
    }

    public java.lang.String getName() {
        return _Name;
    }

    public void setName(java.lang.String value) {
        _Name = value;
    }

    public org.publisher.elsevier.xml.impl.runtime.UnmarshallingEventHandler createUnmarshaller(org.publisher.elsevier.xml.impl.runtime.UnmarshallingContext context) {
        return new org.publisher.elsevier.xml.impl.FileImpl.Unmarshaller(context);
    }

    public void serializeBody(org.publisher.elsevier.xml.impl.runtime.XMLSerializer context)
        throws org.xml.sax.SAXException
    {
        context.startElement("", "file");
        context.endNamespaceDecls();
        context.startAttribute("", "size");
        try {
            context.text(((java.lang.String) _Size), "Size");
        } catch (java.lang.Exception e) {
            org.publisher.elsevier.xml.impl.runtime.Util.handlePrintConversionException(this, e, context);
        }
        context.endAttribute();
        context.startAttribute("", "md5");
        try {
            context.text(((java.lang.String) _Md5), "Md5");
        } catch (java.lang.Exception e) {
            org.publisher.elsevier.xml.impl.runtime.Util.handlePrintConversionException(this, e, context);
        }
        context.endAttribute();
        context.startAttribute("", "name");
        try {
            context.text(((java.lang.String) _Name), "Name");
        } catch (java.lang.Exception e) {
            org.publisher.elsevier.xml.impl.runtime.Util.handlePrintConversionException(this, e, context);
        }
        context.endAttribute();
        context.endAttributes();
        context.endElement();
    }

    public void serializeAttributes(org.publisher.elsevier.xml.impl.runtime.XMLSerializer context)
        throws org.xml.sax.SAXException
    {
    }

    public void serializeURIs(org.publisher.elsevier.xml.impl.runtime.XMLSerializer context)
        throws org.xml.sax.SAXException
    {
    }

    public java.lang.Class getPrimaryInterface() {
        return (org.publisher.elsevier.xml.File.class);
    }

    public com.sun.msv.verifier.DocumentDeclaration createRawValidator() {
        if (schemaFragment == null) {
            schemaFragment = com.sun.xml.bind.validator.SchemaDeserializer.deserialize((
 "\u00ac\u00ed\u0000\u0005sr\u0000\'com.sun.msv.grammar.trex.ElementPattern\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0001L\u0000"
+"\tnameClasst\u0000\u001fLcom/sun/msv/grammar/NameClass;xr\u0000\u001ecom.sun.msv."
+"grammar.ElementExp\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0002Z\u0000\u001aignoreUndeclaredAttributesL\u0000"
+"\fcontentModelt\u0000 Lcom/sun/msv/grammar/Expression;xr\u0000\u001ecom.sun."
+"msv.grammar.Expression\u00f8\u0018\u0082\u00e8N5~O\u0002\u0000\u0002L\u0000\u0013epsilonReducibilityt\u0000\u0013Lj"
+"ava/lang/Boolean;L\u0000\u000bexpandedExpq\u0000~\u0000\u0003xppp\u0000sr\u0000\u001fcom.sun.msv.gra"
+"mmar.SequenceExp\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xr\u0000\u001dcom.sun.msv.grammar.BinaryExp"
+"\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0002L\u0000\u0004exp1q\u0000~\u0000\u0003L\u0000\u0004exp2q\u0000~\u0000\u0003xq\u0000~\u0000\u0004ppsq\u0000~\u0000\u0007ppsr\u0000 com.s"
+"un.msv.grammar.AttributeExp\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0002L\u0000\u0003expq\u0000~\u0000\u0003L\u0000\tnameClas"
+"sq\u0000~\u0000\u0001xq\u0000~\u0000\u0004ppsr\u0000\u001bcom.sun.msv.grammar.DataExp\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0003L\u0000\u0002d"
+"tt\u0000\u001fLorg/relaxng/datatype/Datatype;L\u0000\u0006exceptq\u0000~\u0000\u0003L\u0000\u0004namet\u0000\u001dL"
+"com/sun/msv/util/StringPair;xq\u0000~\u0000\u0004ppsr\u0000$com.sun.msv.datatype"
+".xsd.NmtokenType\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xr\u0000\"com.sun.msv.datatype.xsd.Toke"
+"nType\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xr\u0000#com.sun.msv.datatype.xsd.StringType\u0000\u0000\u0000\u0000\u0000"
+"\u0000\u0000\u0001\u0002\u0000\u0001Z\u0000\risAlwaysValidxr\u0000*com.sun.msv.datatype.xsd.BuiltinAt"
+"omicType\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xr\u0000%com.sun.msv.datatype.xsd.ConcreteType"
+"\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xr\u0000\'com.sun.msv.datatype.xsd.XSDatatypeImpl\u0000\u0000\u0000\u0000\u0000\u0000"
+"\u0000\u0001\u0002\u0000\u0003L\u0000\fnamespaceUrit\u0000\u0012Ljava/lang/String;L\u0000\btypeNameq\u0000~\u0000\u0017L\u0000\n"
+"whiteSpacet\u0000.Lcom/sun/msv/datatype/xsd/WhiteSpaceProcessor;x"
+"pt\u0000 http://www.w3.org/2001/XMLSchemat\u0000\u0007NMTOKENsr\u00005com.sun.ms"
+"v.datatype.xsd.WhiteSpaceProcessor$Collapse\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xr\u0000,co"
+"m.sun.msv.datatype.xsd.WhiteSpaceProcessor\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xp\u0000sr\u00000"
+"com.sun.msv.grammar.Expression$NullSetExpression\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000x"
+"q\u0000~\u0000\u0004ppsr\u0000\u001bcom.sun.msv.util.StringPair\u00d0t\u001ejB\u008f\u008d\u00a0\u0002\u0000\u0002L\u0000\tlocalNam"
+"eq\u0000~\u0000\u0017L\u0000\fnamespaceURIq\u0000~\u0000\u0017xpq\u0000~\u0000\u001bt\u0000\u0000sr\u0000#com.sun.msv.grammar."
+"SimpleNameClass\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0002L\u0000\tlocalNameq\u0000~\u0000\u0017L\u0000\fnamespaceURIq\u0000"
+"~\u0000\u0017xr\u0000\u001dcom.sun.msv.grammar.NameClass\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xpt\u0000\u0004sizeq\u0000~\u0000"
+"#sq\u0000~\u0000\u000bppq\u0000~\u0000\u0010sq\u0000~\u0000$t\u0000\u0003md5q\u0000~\u0000#sq\u0000~\u0000\u000bppq\u0000~\u0000\u0010sq\u0000~\u0000$t\u0000\u0004nameq\u0000~"
+"\u0000#sq\u0000~\u0000$t\u0000\u0004fileq\u0000~\u0000#sr\u0000\"com.sun.msv.grammar.ExpressionPool\u0000\u0000"
+"\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0001L\u0000\bexpTablet\u0000/Lcom/sun/msv/grammar/ExpressionPool$C"
+"losedHash;xpsr\u0000-com.sun.msv.grammar.ExpressionPool$ClosedHas"
+"h\u00d7j\u00d0N\u00ef\u00e8\u00ed\u001c\u0003\u0000\u0003I\u0000\u0005countB\u0000\rstreamVersionL\u0000\u0006parentt\u0000$Lcom/sun/msv"
+"/grammar/ExpressionPool;xp\u0000\u0000\u0000\u0002\u0001pq\u0000~\u0000\tq\u0000~\u0000\nx"));
        }
        return new com.sun.msv.verifier.regexp.REDocumentDeclaration(schemaFragment);
    }

    public class Unmarshaller
        extends org.publisher.elsevier.xml.impl.runtime.AbstractUnmarshallingEventHandlerImpl
    {


        public Unmarshaller(org.publisher.elsevier.xml.impl.runtime.UnmarshallingContext context) {
            super(context, "------------");
        }

        protected Unmarshaller(org.publisher.elsevier.xml.impl.runtime.UnmarshallingContext context, int startState) {
            this(context);
            state = startState;
        }

        public java.lang.Object owner() {
            return org.publisher.elsevier.xml.impl.FileImpl.this;
        }

        public void enterElement(java.lang.String ___uri, java.lang.String ___local, java.lang.String ___qname, org.xml.sax.Attributes __atts)
            throws org.xml.sax.SAXException
        {
            int attIdx;
            outer:
            while (true) {
                switch (state) {
                    case  11 :
                        revertToParentFromEnterElement(___uri, ___local, ___qname, __atts);
                        return ;
                    case  0 :
                        if (("file" == ___local)&&("" == ___uri)) {
                            context.pushAttributes(__atts, false);
                            state = 1;
                            return ;
                        }
                        break;
                    case  7 :
                        attIdx = context.getAttribute("", "name");
                        if (attIdx >= 0) {
                            final java.lang.String v = context.eatAttribute(attIdx);
                            state = 10;
                            eatText1(v);
                            continue outer;
                        }
                        break;
                    case  1 :
                        attIdx = context.getAttribute("", "size");
                        if (attIdx >= 0) {
                            final java.lang.String v = context.eatAttribute(attIdx);
                            state = 4;
                            eatText2(v);
                            continue outer;
                        }
                        break;
                    case  4 :
                        attIdx = context.getAttribute("", "md5");
                        if (attIdx >= 0) {
                            final java.lang.String v = context.eatAttribute(attIdx);
                            state = 7;
                            eatText3(v);
                            continue outer;
                        }
                        break;
                }
                super.enterElement(___uri, ___local, ___qname, __atts);
                break;
            }
        }

        private void eatText1(final java.lang.String value)
            throws org.xml.sax.SAXException
        {
            try {
                _Name = com.sun.xml.bind.WhiteSpaceProcessor.collapse(value);
            } catch (java.lang.Exception e) {
                handleParseConversionException(e);
            }
        }

        private void eatText2(final java.lang.String value)
            throws org.xml.sax.SAXException
        {
            try {
                _Size = com.sun.xml.bind.WhiteSpaceProcessor.collapse(value);
            } catch (java.lang.Exception e) {
                handleParseConversionException(e);
            }
        }

        private void eatText3(final java.lang.String value)
            throws org.xml.sax.SAXException
        {
            try {
                _Md5 = com.sun.xml.bind.WhiteSpaceProcessor.collapse(value);
            } catch (java.lang.Exception e) {
                handleParseConversionException(e);
            }
        }

        public void leaveElement(java.lang.String ___uri, java.lang.String ___local, java.lang.String ___qname)
            throws org.xml.sax.SAXException
        {
            int attIdx;
            outer:
            while (true) {
                switch (state) {
                    case  11 :
                        revertToParentFromLeaveElement(___uri, ___local, ___qname);
                        return ;
                    case  10 :
                        if (("file" == ___local)&&("" == ___uri)) {
                            context.popAttributes();
                            state = 11;
                            return ;
                        }
                        break;
                    case  7 :
                        attIdx = context.getAttribute("", "name");
                        if (attIdx >= 0) {
                            final java.lang.String v = context.eatAttribute(attIdx);
                            state = 10;
                            eatText1(v);
                            continue outer;
                        }
                        break;
                    case  1 :
                        attIdx = context.getAttribute("", "size");
                        if (attIdx >= 0) {
                            final java.lang.String v = context.eatAttribute(attIdx);
                            state = 4;
                            eatText2(v);
                            continue outer;
                        }
                        break;
                    case  4 :
                        attIdx = context.getAttribute("", "md5");
                        if (attIdx >= 0) {
                            final java.lang.String v = context.eatAttribute(attIdx);
                            state = 7;
                            eatText3(v);
                            continue outer;
                        }
                        break;
                }
                super.leaveElement(___uri, ___local, ___qname);
                break;
            }
        }

        public void enterAttribute(java.lang.String ___uri, java.lang.String ___local, java.lang.String ___qname)
            throws org.xml.sax.SAXException
        {
            int attIdx;
            outer:
            while (true) {
                switch (state) {
                    case  11 :
                        revertToParentFromEnterAttribute(___uri, ___local, ___qname);
                        return ;
                    case  7 :
                        if (("name" == ___local)&&("" == ___uri)) {
                            state = 8;
                            return ;
                        }
                        break;
                    case  1 :
                        if (("size" == ___local)&&("" == ___uri)) {
                            state = 2;
                            return ;
                        }
                        break;
                    case  4 :
                        if (("md5" == ___local)&&("" == ___uri)) {
                            state = 5;
                            return ;
                        }
                        break;
                }
                super.enterAttribute(___uri, ___local, ___qname);
                break;
            }
        }

        public void leaveAttribute(java.lang.String ___uri, java.lang.String ___local, java.lang.String ___qname)
            throws org.xml.sax.SAXException
        {
            int attIdx;
            outer:
            while (true) {
                switch (state) {
                    case  11 :
                        revertToParentFromLeaveAttribute(___uri, ___local, ___qname);
                        return ;
                    case  7 :
                        attIdx = context.getAttribute("", "name");
                        if (attIdx >= 0) {
                            final java.lang.String v = context.eatAttribute(attIdx);
                            state = 10;
                            eatText1(v);
                            continue outer;
                        }
                        break;
                    case  3 :
                        if (("size" == ___local)&&("" == ___uri)) {
                            state = 4;
                            return ;
                        }
                        break;
                    case  6 :
                        if (("md5" == ___local)&&("" == ___uri)) {
                            state = 7;
                            return ;
                        }
                        break;
                    case  1 :
                        attIdx = context.getAttribute("", "size");
                        if (attIdx >= 0) {
                            final java.lang.String v = context.eatAttribute(attIdx);
                            state = 4;
                            eatText2(v);
                            continue outer;
                        }
                        break;
                    case  4 :
                        attIdx = context.getAttribute("", "md5");
                        if (attIdx >= 0) {
                            final java.lang.String v = context.eatAttribute(attIdx);
                            state = 7;
                            eatText3(v);
                            continue outer;
                        }
                        break;
                    case  9 :
                        if (("name" == ___local)&&("" == ___uri)) {
                            state = 10;
                            return ;
                        }
                        break;
                }
                super.leaveAttribute(___uri, ___local, ___qname);
                break;
            }
        }

        public void handleText(final java.lang.String value)
            throws org.xml.sax.SAXException
        {
            int attIdx;
            outer:
            while (true) {
                try {
                    switch (state) {
                        case  11 :
                            revertToParentFromText(value);
                            return ;
                        case  5 :
                            state = 6;
                            eatText3(value);
                            return ;
                        case  8 :
                            state = 9;
                            eatText1(value);
                            return ;
                        case  7 :
                            attIdx = context.getAttribute("", "name");
                            if (attIdx >= 0) {
                                final java.lang.String v = context.eatAttribute(attIdx);
                                state = 10;
                                eatText1(v);
                                continue outer;
                            }
                            break;
                        case  1 :
                            attIdx = context.getAttribute("", "size");
                            if (attIdx >= 0) {
                                final java.lang.String v = context.eatAttribute(attIdx);
                                state = 4;
                                eatText2(v);
                                continue outer;
                            }
                            break;
                        case  4 :
                            attIdx = context.getAttribute("", "md5");
                            if (attIdx >= 0) {
                                final java.lang.String v = context.eatAttribute(attIdx);
                                state = 7;
                                eatText3(v);
                                continue outer;
                            }
                            break;
                        case  2 :
                            state = 3;
                            eatText2(value);
                            return ;
                    }
                } catch (java.lang.RuntimeException e) {
                    handleUnexpectedTextException(value, e);
                }
                break;
            }
        }

    }

}
