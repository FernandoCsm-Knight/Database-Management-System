package crud.indexes.query.tmp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class Term implements Comparable<Term> {
    public final static int KEY_BYTES = 50;
    public final static int BYTES = KEY_BYTES + Integer.BYTES + Long.BYTES;

    private String term;
    public int count;
    public long address;

    public Term(String term, long address) {
        this.term = fit(term);
        this.count = 1;
        this.address = address;
    }

    public Term(byte[] buffer) throws IOException {
        this.fromByteArray(buffer);
    }

    public String getTerm() {
        return this.term;
    }

    public void setTerm(String term) {
        this.term = fit(term);
    }

    public boolean contains(String subterm) {
        return this.term.equals(fit(subterm));
    }

    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeInt(this.count);
        dos.writeLong(this.address);
        dos.write(this.term.getBytes(StandardCharsets.UTF_8));

        byte[] buffer = baos.toByteArray();
        dos.close();
        baos.close();
        return buffer;
    }

    public void fromByteArray(byte[] buffer) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
        DataInputStream dis = new DataInputStream(bais);
        
        this.count = dis.readInt();
        this.address = dis.readLong();
        byte[] bf = new byte[KEY_BYTES];
        dis.read(bf);
        this.term = fit(new String(bf, StandardCharsets.UTF_8));

        dis.close();
        bais.close();
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof Term))
            return false;
        return this.term.equals(((Term)obj).term);
    }

    @Override
    public int compareTo(Term other) {
        return this.term.compareTo(other.term);
    }

    @Override
    public String toString() {
        return "{ \"term\": \"" + unfit(this.term) + "\", \"count\": \"" + this.count + "\" }";
    }

    public static String fit(String str) {
        if(str.length() > KEY_BYTES)
            str = str.substring(0, KEY_BYTES);
        else if(str.length() < KEY_BYTES)
            str += " ".repeat((KEY_BYTES - str.getBytes(StandardCharsets.UTF_8).length)/" ".getBytes(StandardCharsets.UTF_8).length);

        return str;
    }

    public static String unfit(String str) {
        return str.trim();
    }
} 


