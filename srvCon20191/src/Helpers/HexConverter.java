package Helpers;

import javax.xml.bind.DatatypeConverter;

public class HexConverter {
	public static String toHEX( byte[] arrayBytes )
	{	
		return DatatypeConverter.printHexBinary(arrayBytes);
	}

	public static byte[] fromHEX( String stringToHex )
	{	
		return DatatypeConverter.parseHexBinary(stringToHex);
	}
}
