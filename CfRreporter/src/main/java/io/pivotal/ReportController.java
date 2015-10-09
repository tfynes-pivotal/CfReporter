package io.pivotal;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.cloudfoundry.client.lib.CloudCredentials;
import org.cloudfoundry.client.lib.CloudFoundryClient;
import org.cloudfoundry.client.lib.domain.CloudApplication;
import org.cloudfoundry.client.lib.domain.CloudApplication.AppState;
import org.cloudfoundry.client.lib.domain.CloudOrganization;
import org.cloudfoundry.client.lib.domain.CloudSpace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;


@EnableWebSecurity
@Controller
public class ReportController {
	
	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Value("${vcap.services.pcf.credentials.cfusername}")
	private String cfusername;
	
	@Value("${vcap.services.pcf.credentials.cfpassword}")
	private String cfpassword;
	
	@Value("${vcap.services.pcf.credentials.cftarget}")
	private String cftarget;
	
	CloudFoundryClient client = null;
	
	private void init() {
		CloudCredentials credentials = new CloudCredentials(cfusername,cfpassword);
		client = new CloudFoundryClient(credentials, getTargetURL(cftarget),true);
		client.login(); 
	}
	
	@RequestMapping(value="/",method=RequestMethod.GET)
	public String getHomepage(Model model) {
		return "index";
	}
	
	
	@RequestMapping(value="/report",method=RequestMethod.GET)
	public String getReport(Model model) {
		if (client == null) init();
		Map<String,CloudOrganization> spaceTable = new Hashtable();
		List<CloudSpace> spaces = client.getSpaces();
		for ( CloudSpace space : spaces) {			
			spaceTable.put(space.getMeta().getGuid().toString(), space.getOrganization());
//			log.debug("space guid: " + space.getMeta().getGuid().toString() + " : org name: " + space.getOrganization().getName());
		}

		List<CloudApplication> apps = client.getApplications();
		model.addAttribute("apps", apps);
		model.addAttribute("spacetable",spaceTable);
//		for (CloudApplication app : apps) {
//			CloudSpace space = app.getSpace();
//			log.info("space = " + space.getName());
//			log.info("running instances = " + app.getRunningInstances());
//			log.info("state " + app.getState().toString());
//		}
		return "report";
	}
	
	private static URL getTargetURL(String target) {
        try {
            return URI.create(target).toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException("The target URL is not valid: " + e.getMessage());
        }
    }
}
