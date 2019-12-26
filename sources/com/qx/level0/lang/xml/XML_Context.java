package com.qx.level0.lang.xml;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.qx.level0.lang.xml.composer.XML_Composer;
import com.qx.level0.lang.xml.composer.XML_StreamWriter;
import com.qx.level0.lang.xml.handler.type.TypeHandler;
import com.qx.level0.lang.xml.handler.type.XML_TypeCompilationException;
import com.qx.level0.lang.xml.parser.XML_Parser;
import com.qx.level0.lang.xml.parser.XML_ParsingException;
import com.qx.level0.lang.xml.parser.XML_StreamReader;

/**
 * <h1>XML Context</h1>
 * <h2>Syntax</h2>
 * <p>
 * XML context is now supporting a wider syntax:
 * </p>
 * <ul>
 * <li>Only type annotated as <code>isRoot=true</code> are eligible as
 * roots.</li>
 * <li>Field elements are declared with the default syntax
 * <code>{$field_name}:{$type_name}</code></li>
 * <li><b>Contextual naming</b>. Note that <b>it is allowed that types names
 * conflict in the global scope</b>, they just need not to raise conflict on a
 * specific field. For instance, you can have 3 different types called
 * <code>Function</code> as long as no field possible elements includes more
 * than one of them.</li>
 * <li>Reference to the list can be omitted when there is no conflicts. For
 * instance if a JAVA object (XML-called <code>my-object</code>) has 3 lists of
 * elements whose types are different (say: List of View, List of Callback, List
 * of Schematics), them the following syntax is correct:
 * 
 * <pre>
 * {@code
 * 		<my-object>
 * 			<view id="view01"/>
 * 			<view id="view01"/>
 * 			<view id="view01"/>
 * 			<callback func="whatdoyouwanttodo()"/>
 * 		</my-object>
 * }
 * </pre>
 * 
 * </li>
 * </ul>
 * <p>
 * <h2>Implementation notes</h2>
 * <p>
 * All setting (i.e. support of the various syntaxes exposed above)
 * possibilities are hard-compiled when building the type handler.
 * </p>
 * 
 * @author pc
 *
 */
public class XML_Context {

	private boolean isVerbose = false;

	
	private Map<String, TypeHandler> xmlRoots = new HashMap<>();

	private Map<String, TypeHandler> typeMap = new HashMap<>();

	/**
	 * 
	 * @param types
	 * @throws Exception 
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 */
	public XML_Context(Class<?>... types) throws XML_TypeCompilationException {
		super();
		for(Class<?> type : types){
			register(type);
		}
	}
	
	public void setVerbosity(boolean isVerbose) {
		this.isVerbose = isVerbose;
	}
	

	/**
	 * 
	 * @param type
	 * @throws Exception 
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	public void register(Class<?> type) throws XML_TypeCompilationException {
		
		if(!isRegistered(type)){
			try {
				TypeHandler typeHandler = new TypeHandler(type);
				
				// regsiter
				typeMap.put(typeHandler.getClassName(), typeHandler);
				
				// then initialize
				typeHandler.initialize(this);
				
				if(typeHandler.isRoot()) {
					String tag = typeHandler.getXmlTag();
					xmlRoots.put(tag, typeHandler);
					xmlRoots.put("root:"+tag, typeHandler);
				}
				
			} catch (SecurityException e) {
				throw new XML_TypeCompilationException(type, "Failed to initialize due to "+e.getMessage());
			}
		}
	}

	public boolean isRegistered(Class<?> type){
		return typeMap.containsKey(type.getName());
	}

	
	/**
	 * 
	 * @param type
	 * @return
	 */
	public TypeHandler getTypeHandler(Class<?> type){
		return typeMap.get(type.getName());
	}
	
	
	/**
	 * 
	 * @param tag
	 * @return
	 */
	public TypeHandler getXmlRootTypeHandler(String tag) {
		return xmlRoots.get(tag);
	}

	
	/**
	 * 
	 * @param reader
	 * @return
	 * @throws IOException 
	 * @throws Exception
	 */
	public Object deserialize(Reader reader, String filename) throws XML_ParsingException, IOException {
		XML_StreamReader streamReader = new XML_StreamReader(reader, filename, isVerbose);
		Object object = new XML_Parser(this, streamReader, isVerbose).parse();
		streamReader.close();
		return object;
	}

	public Object deserialize(InputStream inputStream, String filename) throws Exception{
		return deserialize(new InputStreamReader(inputStream), filename);
	}
	
	public Object deserialize(File file) throws Exception{
		try(BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file))){
			Object result = deserialize(inputStream, file.getName());
			inputStream.close();
			return result;	
		}
	}
	
	/**
	 * 
	 * @param target the target class
	 * @param name resource name
	 * @throws IOException 
	 * @throws XML_ParsingException 
	 */
	public Object deserializeResource(Class<?> target, String name) throws IOException, XML_ParsingException {
		String filename = target.getName()+" resource: "+name;
		try(InputStream inputStream = target.getResourceAsStream(name)){
			if(inputStream!=null) {
				InputStreamReader reader = new InputStreamReader(new BufferedInputStream(inputStream), StandardCharsets.UTF_8);
				XML_StreamReader streamReader = new XML_StreamReader(reader, filename, isVerbose);
				Object object = new XML_Parser(this, streamReader, isVerbose).parse();
				streamReader.close();
				return object;
			}
			else {
				throw new IOException("Failed to read resource: "+filename);
			}
		}
	}
	

	public void serialize(Object object, Writer writer) throws Exception{
		XML_StreamWriter streamWriter = new XML_StreamWriter(writer);
		new XML_Composer(this, streamWriter).compose(object);
		streamWriter.close();
	}
	
	public void serialize(Object object, OutputStream outputStream) throws Exception{
		serialize(object, new OutputStreamWriter(outputStream));
	}
	
	public void serialize(Object object, File file) throws Exception{
		serialize(object, new FileOutputStream(file));
	}

}
