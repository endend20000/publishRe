package cabbage.publish;

import java.io.File;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

public class XMLReader {
	public static List<Project> getProjects() throws Exception{
        //File file = new File( "d:\\service.xml" );
		//File file = new File(System.getProperty("user.dir")+"/service.xml");
		File file = new File( "src/main/java/service.xml" );
        JAXBContext jaxbContext = JAXBContext.newInstance( Projects.class );
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        Projects persons = (Projects)jaxbUnmarshaller.unmarshal( file );
		return persons.getProjects();
	}
}
