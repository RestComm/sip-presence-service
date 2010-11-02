/**
 * 
 */
package org.mobicents.xdm.server.appusage.oma.xcapdirectory.uri;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.util.BitSet;

/**
 * Code migrated from Apache Commons Codec 1.4, license is Apache License 2.0
 * @author martins
 *
 */
public class UriComponentEncoder {

	public static Charset UTF8_CHARSET = Charset.forName("UTF-8");
	
	/**
     * Radix used in encoding and decoding.
     */
    static final int RADIX = 16;
    
    /**
     * Consider this field final. The next major release may break compatibility and make this field be final.
     */
    protected static byte ESCAPE_CHAR = '%';
    /**
     * BitSet of www-form-url safe characters.
     */
    protected static final BitSet WWW_FORM_URL = new BitSet(256);
    
    // Static initializer for www_form_url
    static {
        // alpha characters
        for (int i = 'a'; i <= 'z'; i++) {
            WWW_FORM_URL.set(i);
        }
        for (int i = 'A'; i <= 'Z'; i++) {
            WWW_FORM_URL.set(i);
        }
        // numeric characters
        for (int i = '0'; i <= '9'; i++) {
            WWW_FORM_URL.set(i);
        }
        // special chars
        WWW_FORM_URL.set('-');
        WWW_FORM_URL.set('_');
        WWW_FORM_URL.set('.');
        WWW_FORM_URL.set('*');
        // blank to be replaced with +
        WWW_FORM_URL.set(' ');
    }
    
	private static final byte[] encodeUrl(BitSet urlsafe, byte[] bytes) {
		if (bytes == null) {
			return null;
		}
		if (urlsafe == null) {
			urlsafe = WWW_FORM_URL;
		}

		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		for (int i = 0; i < bytes.length; i++) {
			int b = bytes[i];
			if (b < 0) {
				b = 256 + b;
			}
			if (urlsafe.get(b)) {
				if (b == ' ') {
					b = '+';
				}
				buffer.write(b);
			} else {
				buffer.write(ESCAPE_CHAR);
				char hex1 = Character.toUpperCase(Character.forDigit((b >> 4) & 0xF, RADIX));
				char hex2 = Character.toUpperCase(Character.forDigit(b & 0xF, RADIX));
				buffer.write(hex1);
				buffer.write(hex2);
			}
		}
		return buffer.toByteArray();
	}

	private static byte[] encode(String s, BitSet allowed) throws NullPointerException {
        if (s == null) {
            throw new NullPointerException("string to encode is null");
        }
        if (allowed == null) {
            throw new NullPointerException("Allowed bitset may not be null");
        }
        return encodeUrl(allowed, s.getBytes(UTF8_CHARSET));
    }
	
	/**
	 * Encodes an HTTP URI Path.
	 * 
	 * @param path
	 * @return
	 * @throws NullPointerException
	 */
	public static String encodePath(String path) throws NullPointerException {
		return new String(encode(path, UriComponentEncoderBitSets.allowed_abs_path));
	}

	/**
	 * Encodes an HTTP URI Segment.
	 * 
	 * @param path
	 * @return
	 * @throws NullPointerException
	 */
	public static String encodeWithinPath(String segment) throws NullPointerException {
		return new String(encode(segment, UriComponentEncoderBitSets.allowed_within_path));
	}
	
	/**
	 * Encodes an HTTP URI Query.
	 * @param query
	 * @return
	 * @throws NullPointerException
	 */
	public static String encodeQuery(String query) throws NullPointerException {
		return new String(encode(query, UriComponentEncoderBitSets.allowed_query));
	}
	
}
