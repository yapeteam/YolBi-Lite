package cn.yapeteam.yolbi.font.slick.util;

import java.io.InputStream;
import java.net.URL;

/**
 * A resource location that searches the classpath
 * 
 * @author kevin
 */
public class ClasspathLocation implements ResourceLocation {
	/**
	 * @see ResourceLocation#getResource(java.lang.String)
	 */
	public URL getResource(String ref) {
		String cpRef = ref.replace('\\', '/');
		return ResourceLoader.class.getClassLoader().getResource(cpRef);
	}

	/**
	 * @see ResourceLocation#getResourceAsStream(java.lang.String)
	 */
	public InputStream getResourceAsStream(String ref) {
		String cpRef = ref.replace('\\', '/');
		return ResourceLoader.class.getClassLoader().getResourceAsStream(cpRef);	
	}

}
