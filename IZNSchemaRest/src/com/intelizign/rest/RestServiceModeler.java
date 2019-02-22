package com.intelizign.rest;

import com.dassault_systemes.platform.restServices.ModelerBase;
import javax.ws.rs.ApplicationPath;

@ApplicationPath("/resources/IZNSchemaModeler")
public class RestServiceModeler
  extends ModelerBase
{
  public Class<?>[] getServices()
  {
    return new Class[] { RestfulCLI.class };
  }
}
