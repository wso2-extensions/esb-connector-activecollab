/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.connector.integration.test.activecollab;

import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.connector.integration.test.base.ConnectorIntegrationTestBase;
import org.wso2.connector.integration.test.base.RestResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ActiveCollabConnectorIntegrationTest extends ConnectorIntegrationTestBase {

	private Map<String, String> esbRequestHeadersMap = new HashMap<String, String>();

	private Map<String, String> apiRequestHeadersMap = new HashMap<String, String>();

	/**
	 * Set up the environment.
	 */
	@BeforeClass(alwaysRun = true)
	public void setEnvironment() throws Exception {

		init("activecollab-connector-2.0.2-SNAPSHOT");

		esbRequestHeadersMap.put("Content-Type", "application/json");
		apiRequestHeadersMap.put("Content-Type", "application/json");

		String apiEndPointForGetIntent = "https://my.activecollab.com/api/" +
		                                 connectorProperties.getProperty("apiVersion") + "/external/login";
		RestResponse<JSONObject> apiRestResponseFromGetIntent =
				sendJsonRestRequest(apiEndPointForGetIntent, "POST", apiRequestHeadersMap, "getIntent_mandatory.json");
		String intent = apiRestResponseFromGetIntent.getBody().getJSONObject("user").getString("intent");
		String apiUrl =
				apiRestResponseFromGetIntent.getBody().getJSONArray("accounts").getJSONObject(0).getString("url");
		connectorProperties.setProperty("intent", intent);
		connectorProperties.setProperty("apiUrl", apiUrl);

		String apiEndPointForGetToken = connectorProperties.getProperty("apiUrl") + "/api/" +
		                                connectorProperties.getProperty("apiVersion") + "/issue-token-intent";
		RestResponse<JSONObject> apiRestResponseFromGetToken =
				sendJsonRestRequest(apiEndPointForGetToken, "POST", apiRequestHeadersMap, "getToken_mandatory.json");
		String token = apiRestResponseFromGetToken.getBody().getString("token");
		apiRequestHeadersMap.put("X-Angie-AuthApiToken", token);
	}

	/**
	 * Positive test case for createCategory method with mandatory parameters.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(enabled = true, description = "activecollab {createCategory} integration test with mandatory parameters.")
	public void testCreateCategoryWithMandatoryParameters() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:createCategory");

		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "createCategory_mandatory.json");
		String categoryId = esbRestResponse.getBody().getJSONObject("single").getString("id");
		connectorProperties.setProperty("categoryId", categoryId);

		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("single").getString("name"),
		                    connectorProperties.getProperty("CategoryName"));
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
	}

	/**
	 * Negative test case for createCategory method.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(groups = { "wso2.esb" }, dependsOnMethods = { "testCreateCategoryWithMandatoryParameters" },
			description = "activecollab {createCategory} integration test negative case.")
	public void testCreateCategoryWithNegativeCase() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:createCategory");
		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "createCategory_negative.json");

		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 500);
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("field_errors").getJSONArray("name").getString(0),
		                    "Name is required");
		Assert.assertEquals(esbRestResponse.getBody().getString("message"), "Validation failed");
	}

	/**
	 * Positive test case for listCategories method with mandatory parameters.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(enabled = true, description = "activecollab {listCategories} integration test with mandatory parameters.")
	public void testListCategoriesWithMandatoryParameters() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:listCategories");

		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "listCategories_mandatory.json");

		String apiEndPoint =
				connectorProperties.getProperty("apiUrl") + "/api/" + connectorProperties.getProperty("apiVersion") +
				"/projects/categories";
		RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);

		Assert.assertEquals(esbRestResponse.getBody().toString(), apiRestResponse.getBody().toString());
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
		Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
	}

	/**
	 * Positive test case for renameCategory method with mandatory parameters.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(enabled = true, dependsOnMethods = { "testCreateCategoryWithMandatoryParameters" },
			description = "activecollab {renameCategory} integration test with mandatory parameters.")
	public void testRenameCategoryWithMandatoryParameters() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:renameCategory");

		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "renameCategory_mandatory.json");

		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("single").getString("name"),
		                    connectorProperties.getProperty("newCategoryName"));
	}

	/**
	 * Negative test case for renameCategory method.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(groups = { "wso2.esb" }, dependsOnMethods = { "testRenameCategoryWithMandatoryParameters" },
			description = "activecollab {renameCategory} integration test negative case.")
	public void testRenameCategoryWithNegativeCase() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:renameCategory");
		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "renameCategory_negative.json");

		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 500);
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("field_errors").getJSONArray("name").getString(0),
		                    "Name is required");
	}

	/**
	 * Positive test case for deleteCategory method with mandatory parameters.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(enabled = true, priority = 1, dependsOnMethods = { "testCreateCategoryWithMandatoryParameters",
	                                                         "testRenameCategoryWithMandatoryParameters",
	                                                         "testRenameCategoryWithNegativeCase" },
			description = "activecollab {deleteCategory} integration test with mandatory  parameters.")
	public void testDeleteCategoryWithMandatoryParameters() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:Category");

		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "deleteCategory_mandatory.json");

		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
	}

	/**
	 * Negative test case for deleteCategory method.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(groups = { "wso2.esb" }, dependsOnMethods = { "testDeleteCategoryWithMandatoryParameters" },
			description = "activecollab {deleteCategory} integration test negative case.")
	public void testDeleteCategoryWithNegativeCase() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:deleteCategory");
		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "deleteCategory_negative.json");

		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 404);
	}

	/**
	 * Positive test case for createCompany method with mandatory parameters.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(enabled = true, description = "activecollab {createCompany} integration test with mandatory parameters.")
	public void testCreateCompanyWithMandatoryParameters() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:createCompany");

		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "createCompany_mandatory.json");

		String companyId = esbRestResponse.getBody().getJSONObject("single").getString("id");
		connectorProperties.setProperty("companyId", companyId);

		String apiEndPoint =
				connectorProperties.getProperty("apiUrl") + "/api/" + connectorProperties.getProperty("apiVersion") +
				"/companies/" + connectorProperties.getProperty("companyId");
		RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);

		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("single").getString("name"),
		                    connectorProperties.getProperty("companyName"));
		Assert.assertEquals(apiRestResponse.getBody().getJSONObject("single").getString("name"),
		                    connectorProperties.getProperty("companyName"));
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
		Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
	}

	/**
	 * Negative test case for createCompany method.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(groups = { "wso2.esb" }, description = "activecollab {createCompany} integration test negative case.")
	public void testCreateCompanyWithNegativeCase() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:createCompany");
		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "createCompany_negative.json");

		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("field_errors").getJSONArray("name").getString(0),
		                    "Value of name field is required");
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 500);
	}

	/**
	 * Positive test case for getCompany method with mandatory parameters.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(enabled = true, dependsOnMethods = { "testCreateCompanyWithMandatoryParameters" },
			description = "activecollab {getCompany} integration test with mandatory parameters.")
	public void testGetCompanyWithMandatoryParameters() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:getCompany");

		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "getCompany_mandatory.json");

		String apiEndPoint =
				connectorProperties.getProperty("apiUrl") + "/api/" + connectorProperties.getProperty("apiVersion") +
				"/companies/" + connectorProperties.getProperty("companyId");
		RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);

		Assert.assertEquals(esbRestResponse.getBody().toString(), apiRestResponse.getBody().toString());
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
		Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
	}

	/**
	 * Negative test case for getCompany method.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(groups = { "wso2.esb" }, dependsOnMethods = { "testGetCompanyWithMandatoryParameters" },
			description = "activecollab {getCompany} integration test negative case.")
	public void testGetCompanyWithNegativeCase() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:getCompany");
		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "getCompany_negative.json");

		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 404);
	}

	/**
	 * Positive test case for listCompanies method with mandatory parameters.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(enabled = true, description = "activecollab {listCompanies} integration test with mandatory parameters.")
	public void testListCompaniesWithMandatoryParameters() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:listCompanies");

		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "listCompanies_mandatory.json");

		String apiEndPoint =
				connectorProperties.getProperty("apiUrl") + "/api/" + connectorProperties.getProperty("apiVersion") +
				"/companies";
		RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);

		Assert.assertEquals(esbRestResponse.getBody().toString(), apiRestResponse.getBody().toString());
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
		Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
	}

	/**
	 * Positive test case for createInvoice method with mandatory parameters.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(enabled = true, dependsOnMethods = { "testCreateCompanyWithMandatoryParameters" },
			description = "activecollab {createInvoice} integration test with mandatory parameters.")
	public void testCreateInvoiceWithMandatoryParameters() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:createInvoice");

		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "createInvoice_mandatory.json");

		String invoiceId = esbRestResponse.getBody().getJSONObject("single").getString("id");
		connectorProperties.setProperty("invoiceId", invoiceId);

		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("single").getString("company_address"),
		                    connectorProperties.getProperty("address"));
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("single").getString("company_id"),
		                    connectorProperties.getProperty("companyId"));
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
	}

	/**
	 * Positive test case for createInvoice method with optional parameters.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(enabled = true, dependsOnMethods = { "testCreateCompanyWithMandatoryParameters" },
			description = "activecollab {createInvoice} integration test with optional parameters.")
	public void testCreateInvoiceWithOptionalParameters() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:createInvoice");

		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "createInvoice_optional.json");

		String invoiceOptionalId = esbRestResponse.getBody().getJSONObject("single").getString("id");
		connectorProperties.setProperty("invoiceOptionalId", invoiceOptionalId);

		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("single").getString("company_address"),
		                    connectorProperties.getProperty("address"));

		Assert.assertEquals(esbRestResponse.getBody().getJSONArray("items").getJSONObject(0).getString("class"),
		                    "InvoiceItem");
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("single").getString("company_address"),
		                    connectorProperties.getProperty("address"));
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
	}

	/**
	 * Negative test case for createInvoice method.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(groups = { "wso2.esb" }, description = "activecollab {createInvoice} integration test negative case.")
	public void testCreateInvoiceWithNegativeCase() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:createInvoice");
		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "createInvoice_negative.json");

		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 500);
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("field_errors").getJSONArray("number").getString(0),
		                    "Value of number field is required");
	}

	/**
	 * Positive test case for getInvoice method with mandatory parameters.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(enabled = true, dependsOnMethods = { "testCreateInvoiceWithMandatoryParameters" },
			description = "activecollab {getInvoice} integration test with mandatory parameters.")
	public void testGetInvoiceWithMandatoryParameters() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:getInvoice");

		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "getInvoice_mandatory.json");

		String apiEndPoint =
				connectorProperties.getProperty("apiUrl") + "/api/" + connectorProperties.getProperty("apiVersion") +
				"/invoices/" + connectorProperties.getProperty("invoiceId");
		RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);

		Assert.assertEquals(esbRestResponse.getBody().toString(), apiRestResponse.getBody().toString());
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
		Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
	}

	/**
	 * Negative test case for getInvoice method.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(groups = { "wso2.esb" }, description = "activecollab {getInvoice} integration test negative case.")
	public void testGetInvoiceWithNegativeCase() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:getInvoice");
		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "getInvoice_negative.json");

		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 404);
	}

	/**
	 * Positive test case for listInvoices method with mandatory parameters.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(enabled = true, description = "activecollab {listInvoices} integration test with mandatory parameters.")
	public void testListInvoicesWithMandatoryParameters() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:listInvoices");

		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "listInvoices_mandatory.json");

		String apiEndPoint =
				connectorProperties.getProperty("apiUrl") + "/api/" + connectorProperties.getProperty("apiVersion") +
				"/invoices";
		RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);

		Assert.assertEquals(esbRestResponse.getBody().toString(), apiRestResponse.getBody().toString());
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
		Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
	}

	/**
	 * Positive test case for sendInvoice method with mandatory parameters.
	 *
	 * @throws JSONException
	 * @throws IOException   /*
	 */
	@Test(enabled = true, dependsOnMethods = { "testCreateInvoiceWithMandatoryParameters" },
			description = "activecollab {sendInvoice} integration test with mandatory parameters.")
	public void testSendInvoiceWithMandatoryParameters() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:sendInvoice");

		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "sendInvoice_mandatory.json");

		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
	}

	/**
	 * Positive test case for sendInvoice method with optional parameters.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(enabled = true, dependsOnMethods = { "testCreateInvoiceWithOptionalParameters" },
			description = "activecollab {sendInvoice} integration test with optional parameters.")
	public void testSendInvoiceWithOptionalParameters() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:sendInvoice");

		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "sendInvoice_optional.json");

		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("single").getString("email_subject"),
		                    connectorProperties.getProperty("subject"));
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("single").getString("email_body"),
		                    connectorProperties.getProperty("message"));
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
	}

	/**
	 * Negative test case for sendInvoice method.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(groups = { "wso2.esb" }, description = "activecollab {sendInvoice} integration test negative case.")
	public void testSendInvoiceWithNegativeCase() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:sendInvoice");
		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "sendInvoice_negative.json");

		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 404);
	}

	/**
	 * Positive test case for deleteInvoice method with mandatory parameters.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(enabled = true, priority = 1, dependsOnMethods = { "testCreateInvoiceWithMandatoryParameters",
	                                                         "testSendInvoiceWithMandatoryParameters",
	                                                         "testListInvoicesWithMandatoryParameters",
	                                                         "testGetInvoiceWithMandatoryParameters" },
			description = "activecollab {deleteInvoice} integration test with mandatory  parameters.")
	public void testDeleteInvoiceWithMandatoryParameters() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:deleteInvoice");

		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "deleteInvoice_mandatory.json");

		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
	}

	/**
	 * Negative test case for deleteInvoice method.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(groups = { "wso2.esb" }, description = "activecollab {deleteInvoice} integration test negative case.")
	public void testDeleteInvoiceWithNegativeCase() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:deleteInvoice");
		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "deleteInvoice_negative.json");

		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 404);
	}

	/**
	 * Positive test case for exportInvoice method with mandatory parameters.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(enabled = true, priority = 1, dependsOnMethods = { "testCreateInvoiceWithMandatoryParameters" },
			description = "activecollab {exportInvoice} integration test with mandatory  parameters.")
	public void testExportInvoiceWithMandatoryParameters() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:exportInvoice");

		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "exportInvoice_mandatory.json");

		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
	}

	/**
	 * Negative test case for exportInvoice method.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(groups = { "wso2.esb" }, description = "activecollab {exportInvoice} integration test negative case.")
	public void testExportInvoiceWithNegativeCase() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:exportInvoice");
		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "exportInvoice_negative.json");

		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 404);
	}

	/**
	 * Positive test case for createUser method with mandatory parameters.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(enabled = true, priority = 1, description = "activecollab {createUser} integration test with mandatory  " +
	                                                  "parameters.")
	public void testCreateUserWithMandatoryParameters() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:createUser");

		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "createUser_mandatory.json");

		String userId = esbRestResponse.getBody().getJSONObject("single").getString("id");
		connectorProperties.setProperty("userId", userId);

		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("single").getString("email"),
		                    connectorProperties.getProperty("userEmail"));
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("single").getString("class"),
		                    connectorProperties.getProperty("type"));
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
	}

	/**
	 * Positive test case for createUser method with optional parameters.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(enabled = true, dependsOnMethods = { "testCreateCompanyWithMandatoryParameters" },
			description = "activecollab {createUser} integration test with optional parameters.")
	public void testCreateUserWithOptionalParameters() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:createUser");

		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "createUser_optional.json");

		String userOptId = esbRestResponse.getBody().getJSONObject("single").getString("id");
		connectorProperties.setProperty("userOptId", userOptId);

		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("single").getString("email"),
		                    connectorProperties.getProperty("userEmailOpt"));
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("single").getString("class"),
		                    connectorProperties.getProperty("type"));
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("single").getString("company_id"),
		                    connectorProperties.getProperty("companyId"));
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
	}

	/**
	 * Negative test case for createUser method.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(groups = { "wso2.esb" }, description = "activecollab {createUser} integration test negative case.")
	public void testCreateUserWithNegativeCase() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:createUser");
		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "createUser_negative.json");

		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 500);
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("field_errors").getJSONArray("email").getString(0),
		                    "Value of email field is required");
	}

	/**
	 * Positive test case for getAllUsers method with mandatory parameters.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(enabled = true, description = "activecollab {getAllUsers} integration test with mandatory parameters.")
	public void testGetAllUsersWithMandatoryParameters() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:getAllUsers");

		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "getAllUsers_mandatory.json");

		String apiEndPoint =
				connectorProperties.getProperty("apiUrl") + "/api/" + connectorProperties.getProperty("apiVersion") +
				"/users/all";
		RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);

		Assert.assertEquals(esbRestResponse.getBody().toString(), apiRestResponse.getBody().toString());
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
		Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
	}

	/**
	 * Positive test case for listUsers method with mandatory parameters.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(enabled = true, description = "activecollab {listUsers} integration test with mandatory parameters.")
	public void testListUsersWithMandatoryParameters() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:listUsers");

		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "listUsers_mandatory.json");

		String apiEndPoint =
				connectorProperties.getProperty("apiUrl") + "/api/" + connectorProperties.getProperty("apiVersion") +
				"/users";
		RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);

		Assert.assertEquals(esbRestResponse.getBody().toString(), apiRestResponse.getBody().toString());
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
		Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
	}

	/**
	 * Positive test case for getUser method with mandatory parameters.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(enabled = true, dependsOnMethods = { "testCreateUserWithMandatoryParameters" },
			description = "activecollab {getUser} integration test with mandatory parameters.")
	public void testGetUserWithMandatoryParameters() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:getUser");

		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "getUser_mandatory.json");

		String apiEndPoint =
				connectorProperties.getProperty("apiUrl") + "/api/" + connectorProperties.getProperty("apiVersion") +
				"/users/" + connectorProperties.getProperty("userId");
		RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);

		Assert.assertEquals(esbRestResponse.getBody().toString(), apiRestResponse.getBody().toString());
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("single").getString("id"),
		                    apiRestResponse.getBody().getJSONObject("single").getString("id"));
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
		Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
	}

	/**
	 * Negative test case for getUser method.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(groups = { "wso2.esb" }, description = "activecollab {getUser} integration test negative case.")
	public void testGetUserWithNegativeCase() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:getUser");
		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "getUser_negative.json");

		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 404);
	}

	/**
	 * Positive test case for deleteUser method with mandatory parameters.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(enabled = true, priority = 1, dependsOnMethods = { "testCreateUserWithMandatoryParameters" },
			description = "activecollab {deleteUser} integration test with mandatory  parameters.")
	public void testDeleteUserWithMandatoryParameters() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:deleteUser");

		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "deleteUser_mandatory.json");

		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("single").getString("is_trashed"), "true");
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("single").getString("id"),
		                    connectorProperties.get("userId"));
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
	}

	/**
	 * Negative test case for deleteUser method.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(groups = { "wso2.esb" }, description = "activecollab {deleteUser} integration test negative case.")
	public void testDeleteUserWithNegativeCase() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:deleteUser");
		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "deleteUser_negative.json");

		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 404);
	}

	/**
	 * Positive test case for reactivateUser method with mandatory parameters.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(enabled = true, priority = 2, dependsOnMethods = { "testCreateUserWithMandatoryParameters",
	                                                         "testDeleteUserWithMandatoryParameters" },
			description = "activecollab {reactivateUser} integration test with mandatory  parameters.")
	public void testReactivateUserWithMandatoryParameters() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:reactivateUser");

		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "reactivateUser_mandatory.json");

		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("single").getString("is_trashed"), "false");
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("single").getString("id"),
		                    connectorProperties.get("userId"));
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
	}

	/**
	 * Negative test case for reactivateUser method.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(groups = { "wso2.esb" }, description = "activecollab {reactivateUser} integration test negative case.")
	public void testReactivateUserWithNegativeCase() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:reactivateUser");
		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "reactivateUser_negative.json");

		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 404);
	}

	/**
	 * Positive test case for createProject method with mandatory parameters.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(enabled = true, description = "activecollab {createProject} integration test with mandatory  parameters.")
	public void testCreateProjectWithMandatoryParameters() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:createProject");

		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "createProject_mandatory.json");

		String projectId = esbRestResponse.getBody().getJSONObject("single").getString("id");
		connectorProperties.setProperty("projectId", projectId);

		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("single").getString("name"),
		                    connectorProperties.getProperty("projectName"));
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("single").getString("is_completed"), "false");
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
	}

	/**
	 * Positive test case for createProject method with optional parameters.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(enabled = true, dependsOnMethods = { "testCreateCompanyWithMandatoryParameters",
	                                           "testCreateCategoryWithMandatoryParameters" },
			description = "activecollab {createProject} integration test with optional parameters.")
	public void testCreateProjectWithOptionalParameters() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:createProject");

		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "createProject_optional.json");

		String projectIdOpt = esbRestResponse.getBody().getJSONObject("single").getString("id");
		connectorProperties.setProperty("projectIdOpt", projectIdOpt);

		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("single").getString("name"),
		                    connectorProperties.getProperty("projectNameOpt"));
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("single").getString("is_completed"), "false");
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("single").getString("category_id"),
		                    connectorProperties.getProperty("categoryId"));
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("single").getString("company_id"),
		                    connectorProperties.getProperty("companyId"));
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
	}

	/**
	 * Negative test case for createProject method.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(groups = { "wso2.esb" }, description = "activecollab {createProject} integration test negative case.")
	public void testCreateProjectWithNegativeCase() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:createProject");
		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "createProject_negative.json");

		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("field_errors").getJSONArray("name").getString(0),
		                    "Value of name field is required");
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 500);
	}

	/**
	 * Positive test case for getProject method with mandatory parameters.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(enabled = true, dependsOnMethods = { "testCreateProjectWithMandatoryParameters" },
			description = "activecollab {getProject} integration test with mandatory parameters.")
	public void testGetProjectWithMandatoryParameters() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:getProject");

		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "getProject_mandatory.json");

		String apiEndPoint =
				connectorProperties.getProperty("apiUrl") + "/api/" + connectorProperties.getProperty("apiVersion") +
				"/projects/" + connectorProperties.getProperty("projectId");
		RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);

		Assert.assertEquals(esbRestResponse.getBody().toString(), apiRestResponse.getBody().toString());
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
		Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
	}

	/**
	 * Negative test case for getProject method.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(groups = { "wso2.esb" }, description = "activecollab {getProject} integration test negative case.")
	public void testGetProjectWithNegativeCase() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:getProject");
		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "getProject_negative.json");

		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 404);
	}

	/**
	 * Positive test case for listProjects method with mandatory parameters.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(enabled = true, dependsOnMethods = { "testCreateProjectWithMandatoryParameters",
	                                           "testCreateProjectWithOptionalParameters" },
			description = "activecollab {listProjects} integration test with mandatory parameters.")
	public void testListProjectsWithMandatoryParameters() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:listProjects");

		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "listProjects_mandatory.json");

		String apiEndPoint =
				connectorProperties.getProperty("apiUrl") + "/api/" + connectorProperties.getProperty("apiVersion") +
				"/projects";
		RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);

		Assert.assertEquals(esbRestResponse.getBody().toString(), apiRestResponse.getBody().toString());
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
		Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
	}

	/**
	 * Positive test case for listProjectNames method with mandatory parameters.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(enabled = true, dependsOnMethods = { "testCreateProjectWithMandatoryParameters",
	                                           "testCreateProjectWithOptionalParameters" },
			description = "activecollab {listProjectNames} integration test with mandatory parameters.")
	public void testListProjectNamesWithMandatoryParameters() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:listProjectNames");

		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "listProjectNames_mandatory.json");

		String apiEndPoint =
				connectorProperties.getProperty("apiUrl") + "/api/" + connectorProperties.getProperty("apiVersion") +
				"/projects/names";
		RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);

		Assert.assertEquals(esbRestResponse.getBody().toString(), apiRestResponse.getBody().toString());
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
		Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
	}

	/**
	 * Positive test case for listCompletedProjects method with mandatory parameters.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(enabled = true, dependsOnMethods = { "testCompleteProjectWithMandatoryParameters" },
			description = "activecollab {listCompletedProjects} integration test with mandatory parameters.")
	public void testListCompletedProjectsWithMandatoryParameters() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:listCompletedProjects");

		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "listCompletedProjects_mandatory.json");

		String apiEndPoint =
				connectorProperties.getProperty("apiUrl") + "/api/" + connectorProperties.getProperty("apiVersion") +
				"/projects/archive";
		RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);

		Assert.assertEquals(esbRestResponse.getBody().toString(), apiRestResponse.getBody().toString());
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
		Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
	}

	/**
	 * Positive test case for completeProject method with mandatory parameters.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(enabled = true, dependsOnMethods = { "testCreateProjectWithMandatoryParameters",
	                                           "testRenameProjectWithMandatoryParameters" },
			description = "activecollab {completeProject} integration test with mandatory  parameters.")
	public void testCompleteProjectWithMandatoryParameters() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:completeProject");

		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "completeProject_mandatory.json");

		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("single").getString("name"),
		                    connectorProperties.getProperty("newProjectName"));
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("single").getString("id"),
		                    connectorProperties.getProperty("projectId"));
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("single").getString("is_completed"), "true");
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
	}

	/**
	 * Negative test case for completeProject method.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(groups = { "wso2.esb" }, description = "activecollab {completeProject} integration test negative case.")
	public void testCompleteProjectWithNegativeCase() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:completeProject");
		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "completeProject_negative.json");

		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 404);
	}

	/**
	 * Positive test case for renameProject method with mandatory parameters.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(enabled = true, dependsOnMethods = { "testCreateProjectWithMandatoryParameters" },
			description = "activecollab {renameProject} integration test with mandatory  parameters.")
	public void testRenameProjectWithMandatoryParameters() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:renameProject");

		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "renameProject_mandatory.json");

		String apiEndPoint =
				connectorProperties.getProperty("apiUrl") + "/api/" + connectorProperties.getProperty("apiVersion") +
				"/projects/" + connectorProperties.getProperty("projectId");
		RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);

		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("single").getString("name"),
		                    apiRestResponse.getBody().getJSONObject("single").getString("name"));
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("single").getString("id"),
		                    apiRestResponse.getBody().getJSONObject("single").getString("id"));
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
	}

	/**
	 * Negative test case for renameProject method.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(groups = { "wso2.esb" }, description = "activecollab {renameProject} integration test negative case.")
	public void testRenameProjectWithNegativeCase() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:renameProject");
		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "renameProject_negative.json");

		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("field_errors").getJSONArray("name").getString(0),
		                    "Value of name field is required");
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 500);
	}

	/**
	 * Positive test case for listCurrencies method with mandatory parameters.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(enabled = true, description = "activecollab {listCurrencies} integration test with mandatory parameters.")
	public void testListCurrenciesWithMandatoryParameters() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:listCurrencies");

		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "listCurrencies_mandatory.json");

		String apiEndPoint =
				connectorProperties.getProperty("apiUrl") + "/api/" + connectorProperties.getProperty("apiVersion") +
				"/currencies";
		RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);

		Assert.assertEquals(esbRestResponse.getBody().toString(), apiRestResponse.getBody().toString());
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
		Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
	}

	/**
	 * Positive test case for createTask method with mandatory parameters.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(enabled = true, dependsOnMethods = { "testCreateProjectWithMandatoryParameters" },
			description = "activecollab {createTask} integration test with mandatory parameters.")
	public void testCreateTaskWithMandatoryParameters() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:createTask");

		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "createTask_mandatory.json");

		String taskId = esbRestResponse.getBody().getJSONObject("single").getString("id");
		connectorProperties.setProperty("taskId", taskId);

		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("single").getString("name"),
		                    connectorProperties.getProperty("taskName"));
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
	}

	/**
	 * Positive test case for createTask method with optional parameters.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(enabled = true, dependsOnMethods = { "testCreateUserWithMandatoryParameters",
	                                           "testCreateProjectWithMandatoryParameters" },
			description = "activecollab {createTask} integration test with optional parameters.")
	public void testCreateTaskWithOptionalParameters() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:createTask");

		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "createTask_optional.json");

		String taskIdOpt = esbRestResponse.getBody().getJSONObject("single").getString("id");
		connectorProperties.setProperty("taskIdOpt", taskIdOpt);

		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("single").getString("name"),
		                    connectorProperties.getProperty("taskNameOpt"));
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("single").getString("assignee_id"),
		                    connectorProperties.getProperty("userId"));
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
	}

	/**
	 * Negative test case for createTask method.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(groups = { "wso2.esb" }, dependsOnMethods = { "testCreateTaskWithMandatoryParameters" },
			description = "activecollab {createTask} integration test negative case.")
	public void testCreateTaskWithNegativeCase() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:createTask");
		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "createTask_negative.json");

		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 500);
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("field_errors").getJSONArray("name").getString(0),
		                    "Task summary is required");
		Assert.assertEquals(esbRestResponse.getBody().getString("message"), "Validation failed");
	}

	/**
	 * Positive test case for getTask method with mandatory parameters.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(enabled = true, dependsOnMethods = { "testCreateProjectWithMandatoryParameters",
	                                           "testCreateTaskWithMandatoryParameters" },
			description = "activecollab {getTask} integration test with mandatory parameters.")
	public void testGetTaskWithMandatoryParameters() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:getTask");

		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "getTask_mandatory.json");

		String apiEndPoint =
				connectorProperties.getProperty("apiUrl") + "/api/" + connectorProperties.getProperty("apiVersion") +
				"/projects/" + connectorProperties.getProperty("projectId") + "/tasks/" +
				connectorProperties.getProperty("taskId");
		RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);

		Assert.assertEquals(esbRestResponse.getBody().toString(), apiRestResponse.getBody().toString());
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
		Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
	}

	/**
	 * Negative test case for getTask method.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(groups = { "wso2.esb" }, dependsOnMethods = { "testGetTaskWithMandatoryParameters" },
			description = "activecollab {getTask} integration test negative case.")
	public void testGetTaskWithNegativeCase() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:getTask");
		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "getTask_negative.json");

		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 404);
	}

	/**
	 * Positive test case for listTasks method with mandatory parameters.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(enabled = true, dependsOnMethods = { "testCreateProjectWithMandatoryParameters",
	                                           "testCreateTaskWithMandatoryParameters" },
			description = "activecollab {listTasks} integration test with mandatory parameters.")
	public void testListTasksWithMandatoryParameters() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:listTasks");

		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "listTasks_mandatory.json");

		String apiEndPoint =
				connectorProperties.getProperty("apiUrl") + "/api/" + connectorProperties.getProperty("apiVersion") +
				"/projects/" + connectorProperties.getProperty("projectId") + "/tasks";
		RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);

		Assert.assertEquals(esbRestResponse.getBody().toString(), apiRestResponse.getBody().toString());
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
		Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
	}

	/**
	 * Positive test case for assignTask method with mandatory parameters.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(enabled = true, dependsOnMethods = { "testCreateProjectWithMandatoryParameters",
	                                           "testCreateTaskWithMandatoryParameters",
	                                           "testCreateUserWithOptionalParameters" },
			description = "activecollab {assignTask} integration test with mandatory parameters.")
	public void testAssignTaskWithMandatoryParameters() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:assignTask");

		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "assignTask_mandatory.json");

		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("single").getString("assignee_id"),
		                    connectorProperties.getProperty("userOptId"));
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
	}

	/**
	 * Negative test case for assignTask method.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(groups = { "wso2.esb" }, description = "activecollab {assignTask} integration test negative case.")
	public void testAssignTaskWithNegativeCase() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:assignTask");
		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "assignTask_negative.json");

		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 404);
	}

	/**
	 * Positive test case for renameTask method with mandatory parameters.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(enabled = true, dependsOnMethods = { "testCreateProjectWithMandatoryParameters",
	                                           "testCreateTaskWithMandatoryParameters" },
			description = "activecollab {renameTask} integration test with mandatory parameters.")
	public void testRenameTaskWithMandatoryParameters() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:renameTask");

		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "renameTask_mandatory.json");

		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("single").getString("name"),
		                    connectorProperties.getProperty("newTaskName"));
	}

	/**
	 * Negative test case for renameTask method.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(groups = { "wso2.esb" }, description = "activecollab {renameTask} integration test negative case.")
	public void testRenameTaskWithNegativeCase() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:renameTask");
		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "renameTask_negative.json");

		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 500);
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("field_errors").getJSONArray("name").getString(0),
		                    "Task summary is required");
	}

	/**
	 * Positive test case for createSubTask method with mandatory parameters.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(enabled = true, dependsOnMethods = { "testCreateProjectWithMandatoryParameters",
	                                           "testCreateTaskWithMandatoryParameters" },
			description = "activecollab {createSubTask} integration test with mandatory parameters.")
	public void testCreateSubTaskWithMandatoryParameters() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:createSubTask");

		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "createSubTask_mandatory.json");

		String subTaskId = esbRestResponse.getBody().getJSONObject("single").getString("id");
		connectorProperties.setProperty("subTaskId", subTaskId);

		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("single").getString("name"),
		                    connectorProperties.getProperty("subTaskName"));
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("single").getString("task_id"),
		                    connectorProperties.getProperty("taskId"));
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("single").getString("project_id"),
		                    connectorProperties.getProperty("projectId"));
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
	}

	/**
	 * Positive test case for createSubTask method with optional parameters.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(enabled = true, dependsOnMethods = { "testCreateUserWithMandatoryParameters",
	                                           "testCreateProjectWithMandatoryParameters",
	                                           "testCreateUserWithOptionalParameters" },
			description = "activecollab {createSubTask} integration test with optional parameters.")
	public void testCreateSubTaskWithOptionalParameters() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:createSubTask");

		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "createSubTask_optional.json");

		String subTaskIdOpt = esbRestResponse.getBody().getJSONObject("single").getString("id");
		connectorProperties.setProperty("subTaskIdOpt", subTaskIdOpt);

		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("single").getString("name"),
		                    connectorProperties.getProperty("subTaskName"));
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("single").getString("task_id"),
		                    connectorProperties.getProperty("taskId"));
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("single").getString("project_id"),
		                    connectorProperties.getProperty("projectId"));
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("single").getString("assignee_id"),
		                    connectorProperties.getProperty("userId"));
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
	}

	/**
	 * Negative test case for createSubTask method.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(groups = { "wso2.esb" }, description = "activecollab {createSubTask} integration test negative case.")
	public void testCreateSubTaskWithNegativeCase() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:createSubTask");
		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "createSubTask_negative.json");

		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 500);
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("field_errors").getJSONArray("body").getString(0),
		                    "Subtask text is required");
	}

	/**
	 * Positive test case for promoteSubTaskToTask method with mandatory parameters.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(enabled = true, dependsOnMethods = { "testCreateProjectWithMandatoryParameters",
	                                           "testCreateTaskWithMandatoryParameters" },
			description = "activecollab {promoteSubTaskToTask} integration test with mandatory parameters.")
	public void testPromoteSubTaskToTaskWithMandatoryParameters() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:promoteSubTaskToTask");

		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "promoteSubTaskToTask_mandatory.json");

		String newTaskId = esbRestResponse.getBody().getJSONObject("single").getString("id");
		connectorProperties.setProperty("newTaskId", newTaskId);

		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("single").getString("name"),
		                    connectorProperties.getProperty("subTaskName"));
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("single").getString("project_id"),
		                    connectorProperties.getProperty("projectId"));
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
	}

	/**
	 * Negative test case for promoteSubTaskToTask method.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(groups = { "wso2.esb" }, description = "activecollab {promoteSubTaskToTask} integration test negative case.")
	public void testPromoteSubTaskToTaskWithNegativeCase() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:promoteSubTaskToTask");
		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "promoteSubTaskToTask_negative.json");

		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 404);
	}

	/**
	 * Positive test case for completeTask method with mandatory parameters.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(enabled = true, dependsOnMethods = { "testCreateProjectWithMandatoryParameters",
	                                           "testCreateTaskWithMandatoryParameters" },
			description = "activecollab {completeTask} integration test with mandatory  parameters.")
	public void testCompleteTaskWithMandatoryParameters() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:completeTask");

		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "completeTask_mandatory.json");

		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("single").getString("name"),
		                    connectorProperties.getProperty("taskName"));
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("single").getString("id"),
		                    connectorProperties.getProperty("taskId"));
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("single").getString("is_completed"), "true");
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
	}

	/**
	 * Negative test case for completeTask method.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(groups = { "wso2.esb" }, description = "activecollab {completeTask} integration test negative case.")
	public void testCompleteTaskWithNegativeCase() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:completeTask");
		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "completeTask_negative.json");

		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 404);
	}

	/**
	 * Positive test case for deleteTask method with mandatory parameters.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(enabled = true, priority = 1, dependsOnMethods = { "testCompleteTaskWithMandatoryParameters" },
			description = "activecollab {deleteTask} integration test with mandatory  parameters.")
	public void testDeleteTaskWithMandatoryParameters() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:deleteTask");

		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "deleteTask_mandatory.json");

		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("single").getString("id"),
		                    connectorProperties.getProperty("taskId"));
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("single").getString("is_trashed"), "true");
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
	}

	/**
	 * Negative test case for deleteTask method.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(groups = { "wso2.esb" }, description = "activecollab {deleteCategory} integration test negative case.")
	public void testDeleteTaskWithNegativeCase() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:deleteTask");
		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "deleteTask_negative.json");

		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 404);
	}

	/**
	 * Positive test case for reopenTask method with mandatory parameters.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(enabled = true, dependsOnMethods = { "testDeleteTaskWithMandatoryParameters",
	                                           "testCompleteTaskWithMandatoryParameters" },
			description = "activecollab {renameProject} integration test with mandatory  parameters.")
	public void testReopenTaskWithMandatoryParameters() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:reopenTask");

		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "reopenTask_mandatory.json");

		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("single").getString("id"),
		                    connectorProperties.getProperty("taskId"));
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("single").getString("is_completed"), "false");
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
	}

	/**
	 * Negative test case for reopenTask method.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(groups = { "wso2.esb" }, description = "activecollab {reopenTask} integration test negative case.")
	public void testReopenTaskWithNegativeCase() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:reopenTask");
		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "reopenTask_negative.json");

		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 404);
	}
}
