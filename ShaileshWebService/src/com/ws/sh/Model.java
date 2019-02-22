package com.ws.sh;
import javax.ws.rs.ApplicationPath;

import com.dassault_systemes.platform.restServices.ModelerBase;

@ApplicationPath("/resources/Modeller")
public class Model extends ModelerBase{
	  public Class<?>[] getServices()
	  {
	    return new Class[] { Service.class };
	  }
}
