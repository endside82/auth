package com.endside;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import jakarta.annotation.PostConstruct;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.text.NumberFormat;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.Executor;

@SuppressWarnings("ALL")
@Slf4j
@EnableAsync
@EnableCaching
@EnableJpaAuditing
@SpringBootApplication(scanBasePackages={"com.endside"})
public class AuthApplication {

	@PostConstruct
	void started() {
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
	}

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(AuthApplication.class);
		app.run(args);
		printGcTypes();
		printMemoryOptions();
	}

	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@SuppressWarnings("NullableProblems")
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/**").allowedOrigins("http://localhost:3000");
			}
		};
	}

	/**
	 * asynchronous 실행 전략을 사용
	 * Thread pool 커스터마이징을 가능하도록 한다
	 * @return executor
	 */
	@Bean
	public Executor taskExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(5);
		executor.setMaxPoolSize(5);
//		executor.setQueueCapacity(500);
		executor.setThreadNamePrefix("worker thread - ");
		executor.initialize();
		return executor;
	}


	private static void printGcTypes(){
		List<GarbageCollectorMXBean> gcMxBeans = ManagementFactory.getGarbageCollectorMXBeans();
        gcMxBeans.forEach(gcMxBean -> log.info("GcName: {}, ObjectName: {}", gcMxBean.getName(), gcMxBean.getObjectName()));
	}

	private static void printMemoryOptions(){
		Runtime runtime = Runtime.getRuntime();
		final NumberFormat format = NumberFormat.getInstance();
		final long maxMemory = runtime.maxMemory();
		final long allocatedMemory = runtime.totalMemory();
		final long freeMemory = runtime.freeMemory();
		final long kb = 1024;
		final long mb = kb * kb;
		final String mega = " MB";
		log.info("========================== Memory Info ==========================");
		log.info("Free memory: {}" , format.format(freeMemory / mb) + mega);
		log.info("Allocated memory: {}" , format.format(allocatedMemory / mb) + mega);
		log.info("Max memory: {}" , format.format(maxMemory / mb) + mega);
		log.info("Total free memory: {}" , format.format((freeMemory + (maxMemory - allocatedMemory)) / mb) + mega);
		log.info("=================================================================\n");
	}


}
