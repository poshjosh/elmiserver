package com.bc.amex;

import com.bc.amex.form.FormConfiguration;
import com.bc.amex.jpa.JpaConfiguration;
import com.bc.app.spring.BcspringbootConfiguration;
import com.looseboxes.mswordbox.MsKioskSetup;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import com.bc.app.spring.lifecycle.LifecycleConfiguration;
import com.bc.config.Config;
import com.bc.elmi.pu.DateConverterJpa;
import com.looseboxes.mswordbox.AppContext;
import com.looseboxes.mswordbox.MsKioskSetupDev;
import com.looseboxes.mswordbox.config.ConfigFactory;
import com.looseboxes.mswordbox.config.ConfigFactoryImpl;
import com.looseboxes.mswordbox.config.ConfigNamesInternal;
import com.looseboxes.mswordbox.config.ConfigService;
import com.looseboxes.mswordbox.exceptions.StartupException;
import com.looseboxes.mswordbox.launchers.LauncherFactory;
import java.util.stream.Collectors;
import javax.swing.JOptionPane;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import java.nio.file.Paths;
import java.time.ZoneOffset;
import java.util.TimeZone;
import javax.annotation.PostConstruct;

/**
 * @author Chinomso Bassey Ikwuagwu on Mar 21, 2019 11:32:36 AM
 */
//@SpringBootApplication
@Configuration
@ComponentScan(basePackages = {"com.bc.app.spring", "com.bc.amex"})//, "com.looseboxes.mswordbox"})
@EnableAutoConfiguration
//@Import({ LifecycleConfiguration.class })
public class Application {

    private transient static final Logger LOG = LoggerFactory.getLogger(Application.class);
    
    @PostConstruct
    public void init(){
        LOG.debug("Default TimeZone: {}", TimeZone.getDefault());
        DateConverterJpa.init(
                TimeZone.getTimeZone(ZoneOffset.UTC), 
                TimeZone.getDefault(), 
//                DateConverterJpa.CONVERT_DATABASE_TO_ENTITY_VALUE);
                DateConverterJpa.CONVERT_NONE);
    }
  
    public static void main(String[] args) {
        
        try{
            
//            final AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
//            ctx.register(otherCfgClasses);
//            ctx.register(mswordboxCfgClasses);
            
            final SpringApplicationBuilder appBuilder = new SpringApplicationBuilder(Application.class);
            
            final Class<?> [] elmiConfigClasses = {
                LifecycleConfiguration.class, BcspringbootConfiguration.class, 
                AmexConfiguration.class, JpaConfiguration.class, FormConfiguration.class};
            addSources(appBuilder, elmiConfigClasses);

            final MsKioskSetup msKioskSetup = new MsKioskSetupDev(
                    Paths.get(System.getProperty("user.home"), "elmiserver", "mskiosk"),
                    () -> LauncherFactory.Type.None
            );
            final Class<?> [] msKioskCfgClasses = msKioskSetup.init();
            addSources(appBuilder, msKioskCfgClasses);
            
            final ConfigurableApplicationContext ctx = appBuilder
                    .headless(false)//msKioskCfgClasses.length == 0)
                    .initializers(msKioskSetup)
                    .run(args);
            
            ctx.registerShutdownHook();
            
            LOG.info("Registered application context shutdownhook");

            final String [] activeProfiles = ctx.getEnvironment().getActiveProfiles();

            final String webAppType = ctx.getEnvironment().getProperty("spring.main.web-application-type");
            
            LOG.info("Spring Context initialized. Web application type: {}, active profiles: {}", 
                    webAppType,
                    (activeProfiles==null?null:Arrays.toString(activeProfiles)));

            if(msKioskSetup.isSetup()) {

                final AppContext appContext;
                
                if(msKioskSetup.getLaunchType() == LauncherFactory.Type.Background) {
                
                    appContext = ctx.getBean(AppContext.class);
                    
                }else {
                
                    final ConfigFactory configFactory = new ConfigFactoryImpl(msKioskSetup.getHomeDir());
                    final Config config = configFactory.getConfig(ConfigService.APP_INTERNAL);
                    final boolean flag = Arrays.asList(activeProfiles).contains("dev");
                    LOG.info("Exit on system exit: {}", flag);
                    
                    config.setBoolean(ConfigNamesInternal.EXIT_SYSTEM_ON_UI_EXIT, flag);
                    configFactory.saveConfig(config, ConfigService.APP_INTERNAL);

                    appContext = msKioskSetup.launchApp();
                }
                
                if(msKioskSetup.isAdmin()) {
                    
                    appContext.startSocketServerAsync();
                }
            }else{

                throw new StartupException("You selected to quit");
            }
        }catch(Throwable e){
            
            try{
                
                e.printStackTrace();

                final String msg = "Encountered an unexpected problem while starting the application";

                LOG.warn(msg, e);

                Object errMsg = e.getLocalizedMessage() == null ? "" : 
                        e.getLocalizedMessage().length() <= 100 ? e.getLocalizedMessage() :
                        e.getLocalizedMessage().substring(0, 100) + "...";

                JOptionPane.showMessageDialog(null, msg + "\n" + errMsg, 
                        "Startup Error", JOptionPane.WARNING_MESSAGE);
            }finally{
                System.exit(1);
            }
        }    
    }
    
    public static void addSources(SpringApplicationBuilder appBuilder, Class... arr) {
    
        if(arr == null || arr.length == 0) {
            return;
        }
        
        final ConfigurationEditor configEditor = new ConfigurationEditor();

        appBuilder.sources(Arrays.asList(arr).stream().map(configEditor).collect(Collectors.toList()).toArray(new Class[0]));
    }
}