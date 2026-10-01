/*
 * Copyright (C) 2025-2026 the Jasper Server OS Authors
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * Copyright (C) 2005-2023. Cloud Software Group, Inc. All Rights Reserved.
 * http://www.jaspersoft.com.
 *
 * Unless you have purchased a commercial license agreement from Jaspersoft,
 * the following license terms apply:
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.jaspersoft.jasperserver.test;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;

import com.jaspersoft.jasperserver.api.common.domain.ExecutionContext;
import com.jaspersoft.jasperserver.api.common.domain.ValidationDetail;
import com.jaspersoft.jasperserver.api.common.domain.ValidationResult;
import com.jaspersoft.jasperserver.api.common.domain.impl.ExecutionContextImpl;
import com.jaspersoft.jasperserver.api.engine.common.domain.Result;
import com.jaspersoft.jasperserver.api.engine.jasperreports.domain.impl.ReportUnitRequest;
import com.jaspersoft.jasperserver.api.engine.jasperreports.domain.impl.ReportUnitResult;
import com.jaspersoft.jasperserver.api.engine.jasperreports.domain.impl.TrialReportUnitRequest;
import com.jaspersoft.jasperserver.api.metadata.common.domain.FileResource;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Resource;
import com.jaspersoft.jasperserver.api.metadata.common.domain.ResourceReference;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.ReportUnit;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.BeanReportDataSource;
import com.jaspersoft.jasperserver.util.test.BaseServiceSetupTestNG;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;
import static org.testng.AssertJUnit.*;

/**
 * @author Lucian Chirita (lucianc@users.sourceforge.net)
 * @version $Id$
 */
public class EngineServiceTestsTestNG extends BaseServiceSetupTestNG  {
	private ExecutionContext m_context;
    protected static Log m_logger = LogFactory.getLog(EngineServiceTestsTestNG.class);

	public EngineServiceTestsTestNG(){
        m_logger.info("EngineServiceTestsTestNG => constructor() called");
    }

    @BeforeClass()
    public void onSetUp() throws Exception {
        m_logger.info("EngineServiceTestsTestNG => onSetUp() called");

        // create an execution context for these tests
        m_context = new ExecutionContextImpl();

        // create resources for these Engine Service tests
        createBeanDS();
        createTableModelDS();
        createCustomDSReportTemplate();
        createCustomDSReport();
        createTableModelDSReport();
        // the v7 reports come before the v6 ones because they're actually more fundamental,
        // with the v6 reports requiring on-the-fly conversion
        createV7BarbecueReportTemplate();
        createV7BarbecueReport();
        createV6BarbecueReportTemplate();
        createV6BarbecueReport();
        createV7ChartReportTemplate();
        createV7ChartReport();
        createV6ChartReportTemplate();
        createV6ChartReport();
    }

    @AfterClass()
    public void onTearDown() throws Exception {
        m_logger.info("EngineServiceTestsTestNG => onTearDown() called");
        setAuthenticatedUser(BaseServiceSetupTestNG.USER_JASPERADMIN);

        // delete resources for these Engine Service tests
        // (as usual, we delete resources in the opposite order they were created)
        deleteV6ChartReport();
        deleteV6ChartReportTemplate();
        deleteV7ChartReport();
        deleteV7ChartReportTemplate();
        deleteV6BarbecueReport();
        deleteV6BarbecueReportTemplate();
        deleteV7BarbecueReport();
        deleteV7BarbecueReportTemplate();
        deleteTableModelDSReport();
        deleteCustomDSReport();
        deleteCustomDSReportTemplate();
        deleteTableModelDS();
        deleteBeanDS();
    }

    /**
     *  doExecuteTest
     */
    @Test()
	public void doExecuteTest() throws Exception	{
        m_logger.info("EngineServiceTestsTestNG => doExecuteTest() called");
        setAuthenticatedUser(BaseServiceSetupTestNG.USER_JASPERADMIN);

		// make a reportunit, execute it, and delete it
		FileResource reportRes = (FileResource) getRepositoryService().newResource(null, FileResource.class);
		reportRes.setFileType(FileResource.TYPE_JRXML);
		setCommon(reportRes, "EmployeesJRXML");
		InputStream jrxml = getClass().getResourceAsStream("/reports/jasper/Employees.jrxml");
		reportRes.readData(jrxml);
		ReportUnit unit = (ReportUnit) getRepositoryService().newResource(null, ReportUnit.class);
		unit.setName("Employees_JDBC");
		unit.setLabel("Employees_JDBC");
		unit.setParentFolder("/reports/samples");
		unit.setDataSourceReference("/datasources/JServerJdbcDS");
		unit.setMainReport(reportRes);

		getRepositoryService().saveResource(null, unit);

		ReportUnitResult result = (ReportUnitResult) getEngineService().execute(m_context, new ReportUnitRequest("/reports/samples/Employees_JDBC", new HashMap()));
		assertNotNull(result);
		JasperPrint jasperPrint = result.getJasperPrint();
		assertNotNull(jasperPrint);
		List pages = jasperPrint.getPages();
		assertNotNull(pages);
		assertTrue(pages.size() > 0);

		getRepositoryService().deleteResource(null,"/reports/samples/Employees_JDBC");
	}

    /**
     *  doGetResourcesTest
     */
    @Test()
	public void doGetResourcesTest() throws Exception	{
        m_logger.info("EngineServiceTestsTestNG => doGetResourcesTest() called");
        setAuthenticatedUser(BaseServiceSetupTestNG.USER_JASPERADMIN);
		ReportUnit reportUnit = (ReportUnit) getRepositoryService().getResource(m_context, "/reports/samples/AllAccounts");
		assertNotNull(reportUnit);
		ResourceReference reportRef = reportUnit.getMainReport();
		assertNotNull(reportRef);
		assertTrue(reportRef.isLocal());
		Resource report = reportRef.getLocalResource();
		assertNotNull(report);
		assertTrue(report instanceof FileResource);
		Resource[] resources = getEngineService().getResources(new ResourceReference(report));
		assertNotNull(resources);
		assertTrue(resources.length == 2);
	}

    /**
     *  doValidateTest
     */
    @Test()
	public void doValidateTest() {
        m_logger.info("EngineServiceTestsTestNG => doValidateTest() called");
		ReportUnit unit = createUnit();

		ValidationResult result = getEngineService().validate(null, unit);
		assertNotNull(result);
		assertEquals(ValidationResult.STATE_ERROR, result.getValidationState());
		List results = result.getResults();
		assertNotNull(results);
		assertTrue(results.size() >= 1);
		ValidationDetail detail = (ValidationDetail) results.get(0);
		assertNotNull(detail);
		assertEquals("SalesByMonthTrialReport", detail.getName());

		addJar(unit);

		result = getEngineService().validate(null, unit);
		assertNotNull(result);
		assertEquals(ValidationResult.STATE_VALID, result.getValidationState());
	}

    /**
     *  doTrialExecuteTest
     */
    @Test()
	public void doTrialExecuteTest() {
        m_logger.info("EngineServiceTestsTestNG => doTrialExecuteTest() called");
        setAuthenticatedUser(BaseServiceSetupTestNG.USER_JASPERADMIN);
		ReportUnit unit = createUnit();
		addJar(unit);

		TrialReportUnitRequest request = new TrialReportUnitRequest(unit, null);
		Result result = getEngineService().execute(null, request);
		assertNotNull(result);
		assertTrue(result instanceof ReportUnitResult);
		ReportUnitResult ruRes = (ReportUnitResult) result;
		JasperPrint print = ruRes.getJasperPrint();
		assertNotNull(print);
		List pages = print.getPages();
		assertNotNull(pages);
		assertTrue(pages.size() > 1);
	}

    /**
     *  doGetMainJasperReportTest
     */
    @Test()
	public void doGetMainJasperReportTest() {
        m_logger.info("EngineServiceTestsTestNG => doGetMainJasperReportTest() called");
        setAuthenticatedUser(BaseServiceSetupTestNG.USER_JASPERADMIN);
		JasperReport jasperReport = getEngineService().getMainJasperReport(null, "/reports/samples/AllAccounts");
		assertNotNull(jasperReport);
		assertEquals("AllAccounts", jasperReport.getName());
	}

    /**
     *  doExecuteWithCustomDsTest
     */
    @Test()
	public void doExecuteWithCustomDataSourceTest() throws Exception	{
        m_logger.info("EngineServiceTestsTestNG => doExecuteWithCustomDataSourceTest() called");
        setAuthenticatedUser(BaseServiceSetupTestNG.USER_JASPERADMIN);
		ReportUnitResult result = (ReportUnitResult) getEngineService().execute(m_context, new ReportUnitRequest("/reports/samples/DataSourceReport", new HashMap()));
		assertNotNull(result);
		JasperPrint jasperPrint = result.getJasperPrint();
		assertNotNull(jasperPrint);
		List pages = jasperPrint.getPages();
		assertNotNull(pages);
		assertTrue(pages.size() > 0);
	}

    /**
     *  doExecuteWithTableModelDataSourceTest
     */
    @Test()
	public void doExecuteWithTableModelDataSourceTest() throws Exception	{
        m_logger.info("EngineServiceTestsTestNG => doExecuteWithTableModelDataSourceTest() called");
        setAuthenticatedUser(BaseServiceSetupTestNG.USER_JASPERADMIN);
		ReportUnitResult result = (ReportUnitResult) getEngineService().execute(m_context, new ReportUnitRequest("/reports/samples/DataSourceTableModel", new HashMap()));
		assertNotNull(result);
		JasperPrint jasperPrint = result.getJasperPrint();
		assertNotNull(jasperPrint);
		List pages = jasperPrint.getPages();
		assertNotNull(pages);
		assertTrue(pages.size() > 0);
	}

    /**
     *  doExecuteV7BarbecueTest
     */
    @Test()
	public void doExecuteV7BarbecueTest() throws Exception	{
        m_logger.info("EngineServiceTestsTestNG => doExecuteV6BarbecueTest() called");
        setAuthenticatedUser(BaseServiceSetupTestNG.USER_JASPERADMIN);
		ReportUnitResult result = (ReportUnitResult) getEngineService().execute(m_context, new ReportUnitRequest("/reports/samples/barbecue7", new HashMap()));
		assertNotNull(result);
		JasperPrint jasperPrint = result.getJasperPrint();
		assertNotNull(jasperPrint);
		List pages = jasperPrint.getPages();
		assertNotNull(pages);
		assertTrue(pages.size() > 0);
	}

    /**
     *  doExecuteV6BarbecueTest
     */
    @Test()
	public void doExecuteV6BarbecueTest() throws Exception	{
        m_logger.info("EngineServiceTestsTestNG => doExecuteV6BarbecueTest() called");
        setAuthenticatedUser(BaseServiceSetupTestNG.USER_JASPERADMIN);
		ReportUnitResult result = (ReportUnitResult) getEngineService().execute(m_context, new ReportUnitRequest("/reports/samples/barbecue6", new HashMap()));
		assertNotNull(result);
		JasperPrint jasperPrint = result.getJasperPrint();
		assertNotNull(jasperPrint);
		List pages = jasperPrint.getPages();
		assertNotNull(pages);
		assertTrue(pages.size() > 0);
	}

    /**
     *  doExecuteV7ChartTest
     */
    @Test()
	public void doExecuteV7ChartTest() throws Exception	{
        m_logger.info("EngineServiceTestsTestNG => doExecuteV6ChartTest() called");
        setAuthenticatedUser(BaseServiceSetupTestNG.USER_JASPERADMIN);
		ReportUnitResult result = (ReportUnitResult) getEngineService().execute(m_context, new ReportUnitRequest("/reports/samples/chart7", new HashMap()));
		assertNotNull(result);
		JasperPrint jasperPrint = result.getJasperPrint();
		assertNotNull(jasperPrint);
		List pages = jasperPrint.getPages();
		assertNotNull(pages);
		assertTrue(pages.size() > 0);
	}

    /**
     *  doExecuteV6ChartTest
     */
    @Test()
	public void doExecuteV6ChartTest() throws Exception	{
        m_logger.info("EngineServiceTestsTestNG => doExecuteV6ChartTest() called");
        setAuthenticatedUser(BaseServiceSetupTestNG.USER_JASPERADMIN);
		ReportUnitResult result = (ReportUnitResult) getEngineService().execute(m_context, new ReportUnitRequest("/reports/samples/chart6", new HashMap()));
		assertNotNull(result);
		JasperPrint jasperPrint = result.getJasperPrint();
		assertNotNull(jasperPrint);
		List pages = jasperPrint.getPages();
		assertNotNull(pages);
		assertTrue(pages.size() > 0);
	}

    private ReportUnit createUnit() {
        ReportUnit unit = (ReportUnit) getRepositoryService().newResource(null,
                ReportUnit.class);
        setCommon(unit, "SalesByMonthTrial");
        unit.setParentFolder("/reports");

        FileResource mainReport = (FileResource) getRepositoryService().newResource(null, FileResource.class);
        mainReport.setFileType(FileResource.TYPE_JRXML);
        setCommon(mainReport, "SalesByMonthTrialReport");
        mainReport.readData(getClass().getResourceAsStream("/reports/jasper/SalesByMonth.jrxml"));
        unit.setMainReport(mainReport);

        unit.setDataSourceReference("/datasources/JServerJdbcDS");

        FileResource img = (FileResource) getRepositoryService().newResource(null, FileResource.class);
        img.setFileType(FileResource.TYPE_IMAGE);
        img.readData(getClass().getResourceAsStream("/images/jasperreports.png"));
        setCommon(img, "Logo");
        unit.addResource(img);

        FileResource subrep = (FileResource) getRepositoryService().newResource(null, FileResource.class);
        subrep.setFileType(FileResource.TYPE_JRXML);
        subrep.readData(getClass().getResourceAsStream("/reports/jasper/SalesByMonthDetail.jrxml"));
        setCommon(subrep, "SalesByMonthDetail");
        unit.addResource(subrep);

        FileResource resBdl = (FileResource) getRepositoryService().newResource(null, FileResource.class);
        resBdl.setFileType(FileResource.TYPE_RESOURCE_BUNDLE);
        resBdl.readData(getClass().getResourceAsStream("/resource_bundles/sales.properties"));
        setCommon(resBdl, "sales.properties");
        unit.addResource(resBdl);

        return unit;
    }

    private void addJar(ReportUnit unit) {
        FileResource jar = (FileResource) getRepositoryService().newResource(null,
                FileResource.class);
        jar.setFileType(FileResource.TYPE_JAR);
        jar.readData(getClass().getResourceAsStream("/jars/scriptlet.jar"));
        setCommon(jar, "Scriptlet");
        unit.addResource(jar);
    }

    private void setCommon(Resource res, String id) {
        res.setName(id);
        res.setLabel(id + "_label");
        res.setDescription(id + " description");
    }

    private void createBeanDS() {
        m_logger.info("EngineServiceTestsTestNG => createBeanDS() is creating /datasources/CustomDSFromBean");

        BeanReportDataSource datasource = (BeanReportDataSource) getUnsecureRepositoryService().newResource(null, BeanReportDataSource.class);
        datasource.setName("CustomDSFromBean");
        datasource.setLabel("Custom data source from a bean");
        datasource.setDescription("A custom data source through a bean");
        datasource.setParentFolder("/datasources");

        datasource.setBeanName("customTestDataSourceService");

        getUnsecureRepositoryService().saveResource(null, datasource);
    }

    private void deleteBeanDS() {
        m_logger.info("EngineServiceTestsTestNG => deleteBeanDS() is deleting /datasources/CustomDSFromBean");
        deleteResource("/datasources/CustomDSFromBean");
    }

    private void createTableModelDS() {
        m_logger.info("EngineServiceTestsTestNG => createTableModelDS() is creating /datasources/CustomTableModelDS");

        BeanReportDataSource datasource = (BeanReportDataSource) getUnsecureRepositoryService().newResource(null, BeanReportDataSource.class);
        datasource.setName("CustomTableModelDS");
        datasource.setLabel("Custom data source from a table model");
        datasource.setDescription("A custom data source through a table model");
        datasource.setParentFolder("/datasources");

        datasource.setBeanName("customTestDataSourceServiceFactory");
        datasource.setBeanMethod("tableModelDataSource");

        getUnsecureRepositoryService().saveResource(null, datasource);
    }

    private void deleteTableModelDS() {
        m_logger.info("EngineServiceTestsTestNG => deleteTableModelDS() is deleting /datasources/CustomTableModelDS");
        deleteResource("/datasources/CustomTableModelDS");
    }

    private void createCustomDSReportTemplate() {
        m_logger.info("EngineServiceTestsTestNG => createCustomDSReportTemplate() is creating /reports/samples/DataSourceReportTemplate");

        FileResource reportRes = (FileResource) getUnsecureRepositoryService().newResource(null, FileResource.class);
        reportRes.setFileType(FileResource.TYPE_JRXML);
        reportRes.setName("DataSourceReportTemplate");
        reportRes.setLabel("Report showing Custom Data Source");
        reportRes.setDescription("Report showing use of Custom Data Source via a bean");
        reportRes.setParentFolder("/reports/samples");

        InputStream jrxml = getClass().getResourceAsStream("/reports/jasper/DataSourceReport.jrxml");
        reportRes.readData(jrxml);

        getUnsecureRepositoryService().saveResource(null, reportRes);
    }

    private void deleteCustomDSReportTemplate() {
        m_logger.info("EngineServiceTestsTestNG => deleteCustomDSReportTemplate() is deleting /reports/samples/DataSourceReportTemplate");
        deleteResource("/reports/samples/DataSourceReportTemplate");
    }

    private void createCustomDSReport() {
        m_logger.info("EngineServiceTestsTestNG => createCustomDSReport() is creating /reports/samples/DataSourceReport");

        ReportUnit unit = (ReportUnit) getUnsecureRepositoryService().newResource(null, ReportUnit.class);
        unit.setName("DataSourceReport");
        unit.setLabel("Report showing Custom Data Source");
        unit.setDescription("Report showing use of Custom Data Source via a bean");
        unit.setParentFolder("/reports/samples");

        unit.setMainReportReference("/reports/samples/DataSourceReportTemplate");
        unit.setDataSourceReference("/datasources/CustomDSFromBean");

        getUnsecureRepositoryService().saveResource(null, unit);
    }

    private void deleteCustomDSReport() {
        m_logger.info("EngineServiceTestsTestNG => deleteCustomDSReport() is deleting /reports/samples/DataSourceReport");
        deleteResource("/reports/samples/DataSourceReport");
    }

    private void createTableModelDSReport() {
        m_logger.info("EngineServiceTestsTestNG => createTableModelDSReport() is creating /reports/samples/DataSourceTableModel");

        ReportUnit unit = (ReportUnit) getUnsecureRepositoryService().newResource(null, ReportUnit.class);
        unit.setName("DataSourceTableModel");
        unit.setLabel("Table Model Data Source");
        unit.setDescription("Report showing use of Custom Data Source via table model");
        unit.setParentFolder("/reports/samples");

        unit.setMainReportReference("/reports/samples/DataSourceReportTemplate");
        unit.setDataSourceReference("/datasources/CustomTableModelDS");
        unit.setMainReportReference("/reports/samples/DataSourceReportTemplate");

        getUnsecureRepositoryService().saveResource(null, unit);
    }

    private void deleteTableModelDSReport() {
        m_logger.info("EngineServiceTestsTestNG => deleteTableModelDSReport() is deleting /reports/samples/DataSourceTableModel");
        deleteResource("/reports/samples/DataSourceTableModel");
    }

    private void createV7BarbecueReportTemplate() {
        m_logger.info("EngineServiceTestsTestNG => createV7BarbecueReportTemplate() is creating /reports/samples/barbecue7ReportTemplate");

        FileResource reportRes = (FileResource) getUnsecureRepositoryService().newResource(null, FileResource.class);
        reportRes.setFileType(FileResource.TYPE_JRXML);
        reportRes.setName("barbecue7ReportTemplate");
        reportRes.setLabel("Jasper 7 Barbecue Report");
        reportRes.setDescription("Jasper 7 report with Barbecue barcodes");
        reportRes.setParentFolder("/reports/samples");

        InputStream jrxml = getClass().getResourceAsStream("/reports/jasper67-new/barbecue7.jrxml");
        reportRes.readData(jrxml);

        getUnsecureRepositoryService().saveResource(null, reportRes);
    }

    private void deleteV7BarbecueReportTemplate() {
        m_logger.info("EngineServiceTestsTestNG => deleteV7BarbecueReportTemplate() is deleting /reports/samples/barbecue7ReportTemplate");
        deleteResource("/reports/samples/barbecue7ReportTemplate");
    }

    private void createV7BarbecueReport() {
        m_logger.info("EngineServiceTestsTestNG => createV7BarbecueReport() is creating /reports/samples/barbecue7");

        ReportUnit unit = (ReportUnit) getUnsecureRepositoryService().newResource(null, ReportUnit.class);
        unit.setName("barbecue7");
        unit.setLabel("Jasper 7 Barbecue Report");
        unit.setDescription("Jasper 7 report with Barbecue barcodes");
        unit.setParentFolder("/reports/samples");

        unit.setMainReportReference("/reports/samples/barbecue7ReportTemplate");

        getUnsecureRepositoryService().saveResource(null, unit);
    }

    private void deleteV7BarbecueReport() {
        m_logger.info("EngineServiceTestsTestNG => deleteV7BarbecueReport() is deleting /reports/samples/barbecue7");
        deleteResource("/reports/samples/barbecue7");
    }

    private void createV6BarbecueReportTemplate() {
        m_logger.info("EngineServiceTestsTestNG => createV6BarbecueReportTemplate() is creating /reports/samples/barbecue6ReportTemplate");

        FileResource reportRes = (FileResource) getUnsecureRepositoryService().newResource(null, FileResource.class);
        reportRes.setFileType(FileResource.TYPE_JRXML);
        reportRes.setName("barbecue6ReportTemplate");
        reportRes.setLabel("Jasper 6 Barbecue Report");
        reportRes.setDescription("Report showing on-the-fly conversion of a Jasper 6 report with Barbecue barcodes");
        reportRes.setParentFolder("/reports/samples");

        InputStream jrxml = getClass().getResourceAsStream("/reports/jasper67-old/barbecue6.jrxml");
        reportRes.readData(jrxml);

        getUnsecureRepositoryService().saveResource(null, reportRes);
    }

    private void deleteV6BarbecueReportTemplate() {
        m_logger.info("EngineServiceTestsTestNG => deleteV6BarbecueReportTemplate() is deleting /reports/samples/barbecue6ReportTemplate");
        deleteResource("/reports/samples/barbecue6ReportTemplate");
    }

    private void createV6BarbecueReport() {
        m_logger.info("EngineServiceTestsTestNG => createV6BarbecueReport() is creating /reports/samples/barbecue6");

        ReportUnit unit = (ReportUnit) getUnsecureRepositoryService().newResource(null, ReportUnit.class);
        unit.setName("barbecue6");
        unit.setLabel("Jasper 6 Barbecue Report");
        unit.setDescription("Report showing on-the-fly conversion of a Jasper 6 report with Barbecue barcodes");
        unit.setParentFolder("/reports/samples");

        unit.setMainReportReference("/reports/samples/barbecue6ReportTemplate");

        getUnsecureRepositoryService().saveResource(null, unit);
    }

    private void deleteV6BarbecueReport() {
        m_logger.info("EngineServiceTestsTestNG => deleteV6BarbecueReport() is deleting /reports/samples/barbecue6");
        deleteResource("/reports/samples/barbecue6");
    }

    private void createV7ChartReportTemplate() {
        m_logger.info("EngineServiceTestsTestNG => createV7ChartReportTemplate() is creating /reports/samples/chart7ReportTemplate");

        FileResource reportRes = (FileResource) getUnsecureRepositoryService().newResource(null, FileResource.class);
        reportRes.setFileType(FileResource.TYPE_JRXML);
        reportRes.setName("chart7ReportTemplate");
        reportRes.setLabel("Jasper 7 Chart Report");
        reportRes.setDescription("Jasper 7 report with a chart");
        reportRes.setParentFolder("/reports/samples");

        InputStream jrxml = getClass().getResourceAsStream("/reports/jasper67-new/chart7.jrxml");
        reportRes.readData(jrxml);

        getUnsecureRepositoryService().saveResource(null, reportRes);
    }

    private void deleteV7ChartReportTemplate() {
        m_logger.info("EngineServiceTestsTestNG => deleteV7ChartReportTemplate() is deleting /reports/samples/chart7ReportTemplate");
        deleteResource("/reports/samples/chart7ReportTemplate");
    }

    private void createV7ChartReport() {
        m_logger.info("EngineServiceTestsTestNG => createV7ChartReport() is creating /reports/samples/chart7");

        ReportUnit unit = (ReportUnit) getUnsecureRepositoryService().newResource(null, ReportUnit.class);
        unit.setName("chart7");
        unit.setLabel("Jasper 7 Chart Report");
        unit.setDescription("Jasper 7 report with a chart");
        unit.setParentFolder("/reports/samples");

        unit.setMainReportReference("/reports/samples/chart7ReportTemplate");

        getUnsecureRepositoryService().saveResource(null, unit);
    }

    private void deleteV7ChartReport() {
        m_logger.info("EngineServiceTestsTestNG => deleteV7ChartReport() is deleting /reports/samples/chart7");
        deleteResource("/reports/samples/chart7");
    }

    private void createV6ChartReportTemplate() {
        m_logger.info("EngineServiceTestsTestNG => createV6ChartReportTemplate() is creating /reports/samples/chart6ReportTemplate");

        FileResource reportRes = (FileResource) getUnsecureRepositoryService().newResource(null, FileResource.class);
        reportRes.setFileType(FileResource.TYPE_JRXML);
        reportRes.setName("chart6ReportTemplate");
        reportRes.setLabel("Jasper 6 Chart Report");
        reportRes.setDescription("Report showing on-the-fly conversion of a Jasper 6 report with a chart");
        reportRes.setParentFolder("/reports/samples");

        InputStream jrxml = getClass().getResourceAsStream("/reports/jasper67-old/chart6.jrxml");
        reportRes.readData(jrxml);

        getUnsecureRepositoryService().saveResource(null, reportRes);
    }

    private void deleteV6ChartReportTemplate() {
        m_logger.info("EngineServiceTestsTestNG => deleteV6ChartReportTemplate() is deleting /reports/samples/chart6ReportTemplate");
        deleteResource("/reports/samples/chart6ReportTemplate");
    }

    private void createV6ChartReport() {
        m_logger.info("EngineServiceTestsTestNG => createV6ChartReport() is creating /reports/samples/chart6");

        ReportUnit unit = (ReportUnit) getUnsecureRepositoryService().newResource(null, ReportUnit.class);
        unit.setName("chart6");
        unit.setLabel("Jasper 6 Chart Report");
        unit.setDescription("Report showing on-the-fly conversion of a Jasper 6 report with a chart");
        unit.setParentFolder("/reports/samples");

        unit.setMainReportReference("/reports/samples/chart6ReportTemplate");

        getUnsecureRepositoryService().saveResource(null, unit);
    }

    private void deleteV6ChartReport() {
        m_logger.info("EngineServiceTestsTestNG => deleteV6ChartReport() is deleting /reports/samples/chart6");
        deleteResource("/reports/samples/chart6");
    }

    private void deleteResource(String uri) {
        Resource result = getRepositoryService().getResource(null, uri);
        assertNotNull(result);
        getRepositoryService().deleteResource(null, uri);
    }

}
