package org.eclipse.tractusx.sde.core.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.eclipse.tractusx.sde.common.exception.ValidationException;
import org.eclipse.tractusx.sde.core.service.SubmodelCsvService;
import org.eclipse.tractusx.sde.core.utils.CsvUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ContextConfiguration(classes = {SubmodelCsvController.class})
@ExtendWith(SpringExtension.class)
class SubmodelCsvControllerTest {
	
	@Autowired
    private SubmodelCsvController submodelCsvController;
    @MockBean
	private SubmodelCsvService submodelCsvService;
    @MockBean
	private CsvUtil csvUtil;
    
    private static final String SAMPLE_TYPE = "sample";
    private static final String TEMPLATE_TYPE = "template";
    
    @Test
    void testGetSubmodelCSVSampleSuccess() throws Exception {

        when(submodelCsvService.findSubmodelCsv((String) any(),(String) any())).thenReturn(new ArrayList<>());
        when(csvUtil.generateCSV(SAMPLE_TYPE,new ArrayList<>())).thenReturn(csvResponseGenerator("aspect"));
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/submodels/csvfile/aspect")
                .param("type", SAMPLE_TYPE)
                .contentType(MediaType.APPLICATION_JSON);
        MockMvcBuilders.standaloneSetup(submodelCsvController)
                .build()
                .perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk());
        }
    
    @Test
    void testGetSubmodelCSVTemplateSuccess() throws Exception {

        when(submodelCsvService.findSubmodelCsv((String) any(),(String) any())).thenReturn(new ArrayList<>());
        when(csvUtil.generateCSV(TEMPLATE_TYPE,new ArrayList<>())).thenReturn(csvResponseGenerator("aspect"));
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/submodels/csvfile/aspect")
                .param("type", TEMPLATE_TYPE)
                .contentType(MediaType.APPLICATION_JSON);
        MockMvcBuilders.standaloneSetup(submodelCsvController)
                .build()
                .perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk());
        }

    @Test
    void testGetSubmodelCSVTypeValidationError() throws Exception {

        when(csvUtil.generateCSV("error",new ArrayList<>())).thenThrow(new ValidationException("Unknown CSV type: error for submodel: aspect"));
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/submodels/csvfile/aspect")
                .param("type", "error")
                .contentType(MediaType.APPLICATION_JSON);
        MockMvcBuilders.standaloneSetup(submodelCsvController)
                .build()
                .perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk());
        }
    
    @Test
    void testGetSubmodelCSVSubmodelNameValidationError() throws Exception {

    	 when(submodelCsvService.findSubmodelCsv("Demo",SAMPLE_TYPE)).thenThrow(new ValidationException("demo submodel is not supported"));
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/submodels/csvfile/aspect")
                .param("type", SAMPLE_TYPE)
                .contentType(MediaType.APPLICATION_JSON);
        MockMvcBuilders.standaloneSetup(submodelCsvController)
                .build()
                .perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk());
        }
    
    @Test
    void testGetDownloadFileByProcessIdSuccess() throws Exception {

    	String processId = "7e4ff341-0a9a-4247-890c-2600f74cc81b";
    	String submodelName = "aspect";
        when(submodelCsvService.findAllSubmodelCsvHistory(submodelName, processId)).thenReturn(new ArrayList<>());
        when(csvUtil.generateCSV(submodelName,new ArrayList<>())).thenReturn(csvResponseGenerator(submodelName));
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/"+submodelName+"/download/"+processId+"/csv")
                .contentType(MediaType.APPLICATION_JSON);
        MockMvcBuilders.standaloneSetup(submodelCsvController)
                .build()
                .perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk());
        }
    
    @Test
    void testGetDownloadFileByProcessIdFailure() throws Exception {

    	String processId = "7e4ff341-0a9a-4247-890c-2600f74cc81b";
    	String submodelName = "demo";
        when(csvUtil.generateCSV(submodelName,new ArrayList<>())).thenThrow(new ValidationException("Unknown CSV type: error for submodel: aspect"));
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/"+submodelName+"/download/"+processId+"/csv")
                .contentType(MediaType.APPLICATION_JSON);
        MockMvcBuilders.standaloneSetup(submodelCsvController)
                .build()
                .perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk());
        }
    
    
	private ResponseEntity<Resource> csvResponseGenerator(String submodelName) {
		
		InputStreamResource file = new InputStreamResource(CsvUtil.writeCsv(new ArrayList<>()));
		ResponseEntity<Resource> response = ResponseEntity.ok()
    			.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename="+submodelName)
    			.contentType(MediaType.parseMediaType("application/csv"))
    			.body(file);
		return response;
	}


}
