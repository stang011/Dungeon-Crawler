package Code;
// Jay Schimmoller

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;

import javax.swing.JOptionPane;

public class Encoder {
	public String encodeObj(Object obj) {
		String encodedObj = "";
		try {
			ByteArrayOutputStream bo = new ByteArrayOutputStream();
		    ObjectOutputStream so = new ObjectOutputStream(bo);
		    so.writeObject(obj);
		    so.flush();
		    encodedObj = Base64.getEncoder().encodeToString(bo.toByteArray());
		    //System.out.println(encodedObj.length());
		    return encodedObj;
		}
		catch(Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.getStackTrace());
			System.exit(0);
		}
		return null;
	}
	
	public Object decodeObj(String s) {
		try {
			byte b[] = Base64.getMimeDecoder().decode(s.getBytes());
		    ByteArrayInputStream bi = new ByteArrayInputStream(b);
		    ObjectInputStream si = new ObjectInputStream(bi);
		    return si.readObject();
		}
		catch(Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.getStackTrace());
			System.exit(0);
		}
		return null;
	}
}