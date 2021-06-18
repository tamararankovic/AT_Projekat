package util;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

import org.jboss.vfs.VirtualFile;

public class ResourceLoader {

	public static File getFile(Class<?> c, String prefix, String fileName) {
		File f = null;
		
		URL url = c.getResource(prefix + fileName);
		
		if (url != null) {
			if (url.toString().startsWith("vfs:/")) {
				try {
					URLConnection conn = new URL(url.toString()).openConnection();
					VirtualFile vf = (VirtualFile)conn.getContent();
					f = vf.getPhysicalFile();
				} catch (Exception ex) {
					ex.printStackTrace();
					f = new File(".");
				}
			} else {
				try {
					f = new File(url.toURI());
				} catch (URISyntaxException e) {
					e.printStackTrace();
					f = new File(".");
				}
			}
		} else {
			f = new File(fileName);
		}	
		return f;
	}
}
