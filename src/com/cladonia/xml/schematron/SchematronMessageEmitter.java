package com.cladonia.xml.schematron;

import net.sf.saxon.serialize.MessageEmitter;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.trans.XPathException;

public class SchematronMessageEmitter extends MessageEmitter implements Receiver {

	public void endDocument() throws XPathException {
        try {
            writer.write('\n');
        } catch (java.io.IOException err) {
            throw new XPathException(err);
        }
        super.close();
    }

    public void close() throws XPathException {
        try {
            if (writer != null) {
                writer.flush();
            }
        } catch (java.io.IOException err) {
            throw new XPathException(err);
        }
    }
}
