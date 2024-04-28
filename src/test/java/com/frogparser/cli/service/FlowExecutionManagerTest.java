package com.frogparser.cli.service;

import com.frogparser.cli.domain.RunTimeVariableDB;
import com.frogparser.cli.domain.flow_execution.AppFlowExecutionSettings;
import com.frogparser.cli.repository.FlowExecutionRepository;
import com.frogparser.cli.repository.RunTimeVariableRepository;
import com.frogparser.cli.test_resource_utils.TestResourceUtils;
import com.frogparser.flow.domain.Flow;
import com.frogparser.flow.domain.FlowContent;
import com.frogparser.flow.executor.domain.FlowExecution;
import com.frogparser.flow.executor.domain.FlowExecutionStateEnum;
import com.frogparser.flow.executor.domain.web_driver_settings.WebDriverSettings;
import com.frogparser.flow.executor.service.FlowExecutorService;
import com.frogparser.flow.executor.service.SeleniumFindByService;
import com.frogparser.flow.executor.service.StringToFlowContentMapper;
import com.frogparser.flow.executor.service.command.AbstractCommandExecutorService;
import com.frogparser.flow.executor.service.os.ZombieProcessKillerService;
import com.frogparser.flow.executor.service.runtime_variable.RunTimeVariableConvertToCSVService;
import com.frogparser.flow.executor.service.validator.AcceptLanguageHeaderValidatorService;
import com.frogparser.flow.executor.service.validator.FlowExecutionIdValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;

@Tag("IntegrationTest")
@SpringBootTest(
        classes = {
                AcceptLanguageHeaderValidatorService.class,
                AppFlowExecutionSettings.class,
                FlowExecutionIdValidator.class,
                FlowExecutionManager.class,
                FlowExecutionRepository.class,
                FlowExecutorService.class,
                FlowVariableSaveServiceImpl.class,
                RunTimeVariableConvertToCSVService.class,
                RunTimeVariableRepository.class,
                SeleniumFindByService.class,
                StringToFlowContentMapper.class,
                WebDriverSettings.class,
                ZombieProcessKillerService.class
        },
        properties = {
                "frog.flow.execution.flowExecutionTimeLimit=36000",
                "frog.flow.maximumCommandExecutionsInFlowLimit=10000000",
                "frog.flow.maximumRecursionDepthLimit=32",
                "frog.flow.maximumRowsInListLimit=100000",
                "frog.flow.maximumRowsInDatasetLimit=100000",
                "frog.selenium.web-driver.window.size.width=1920",
                "frog.selenium.web-driver.window.size.height=1080",
                "frog.selenium.web-driver.timeouts.pageLoadTimeout=60",
                "frog.selenium.web-driver.timeouts.scriptTimeout=60",
                "frog.selenium.web-driver.timeouts.implicitlyWait=60",
                "frog.selenium.web-driver.type=LOCAL_CHROME",
                "frog.selenium.web-driver.local-chrome.driver-executable-path=c:/opt/selenium/chromedriver/123.0.6312.58/chromedriver.exe",
                "frog.selenium.web-driver.local-chrome.executable-path=c:/Program Files/Google/Chrome/Application/chrome.exe",
                "frog.selenium.web-driver.local-chrome.arguments=-disable-extensions -remote-allow-origins=* -user-agent=FrogWebParserBot/1.0 -headless=new -disable-gpu -disk-cache-size=0 -media-cache-size=0 -disable-gpu-shader-disk-cache -incognito",
                "frog.selenium.web-driver.remote-chrome.url=http://localhost:44/wd/hub",
                "frog.selenium.web-driver.remote-chrome.arguments=-disable-extensions -remote-allow-origins=* -user-agent=FrogWebParserBot/1.0 -headless=new -disable-gpu -disk-cache-size=0 -media-cache-size=0 -disable-gpu-shader-disk-cache -incognito",
                "frog.zombie-process-killer.active=false"
        }
)
@ComponentScan(basePackageClasses = {AbstractCommandExecutorService.class})
@EnableConfigurationProperties
public class FlowExecutionManagerTest {

    private final AppFlowExecutionSettings flowExecutionSettings;
    private final WebDriverSettings webDriverSettings;
    private final FlowExecutionManager flowExecutionManager;
    private final StringToFlowContentMapper stringToFlowContentMapper;
    private final FlowExecutionRepository flowExecutionRepository;
    private final RunTimeVariableRepository runTimeVariableRepository;
    private final RunTimeVariableConvertToCSVService runTimeVariableConvertToCSVService;

    @Autowired
    public FlowExecutionManagerTest(AppFlowExecutionSettings flowExecutionSettings,
                                    WebDriverSettings webDriverSettings,
                                    FlowExecutionManager flowExecutionManager,
                                    StringToFlowContentMapper stringToFlowContentMapper,
                                    FlowExecutionRepository flowExecutionRepository,
                                    RunTimeVariableRepository runTimeVariableRepository,
                                    RunTimeVariableConvertToCSVService runTimeVariableConvertToCSVService) {
        this.flowExecutionSettings = flowExecutionSettings;
        this.webDriverSettings = webDriverSettings;
        this.flowExecutionManager = flowExecutionManager;
        this.stringToFlowContentMapper = stringToFlowContentMapper;
        this.flowExecutionRepository = flowExecutionRepository;
        this.runTimeVariableRepository = runTimeVariableRepository;
        this.runTimeVariableConvertToCSVService = runTimeVariableConvertToCSVService;
    }

    @Test
    public void doTest() {

        final String flowContentAsString = TestResourceUtils.readResourceAsString("workflow-examples", "cms", "shopify", "theme", "district", "district-theme-demo-myshopify-com-collections-clothing.json");

        final FlowContent flowContent = stringToFlowContentMapper.map(flowContentAsString);

        final Flow flow = new Flow()
                .setId(1L)
                .setName("district-theme-demo-myshopify-com-collections-clothing")
                .setDescription("district-theme-demo-myshopify-com-collections-clothing")
                .setContent(flowContent);

        final Long flowExecutionId = flowExecutionManager.execute(flow);

        final FlowExecution flowExecution = flowExecutionRepository.findById(flowExecutionId).orElseThrow();

        Assertions.assertEquals(FlowExecutionStateEnum.FINISHED, flowExecution.getState());

        final RunTimeVariableDB runTimeVariableDB = runTimeVariableRepository.findByNameAndFlowExecutionId("dataset", flowExecutionId)
                .orElseThrow();

        final byte[] bytes = runTimeVariableConvertToCSVService.convert(runTimeVariableDB.getValue());

        String datasetAsCSV = new String(bytes);

        System.out.println(datasetAsCSV);

    }

}
