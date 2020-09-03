package com.zhuo.transaction.spring.job;

import com.zhuo.transaction.common.exception.TransactionException;
import com.zhuo.transaction.support.FactoryBuilder;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

/**
 * describe:
 *
 * @author zhuojing
 * @date 2020/09/01
 */
public class TransactionMsgJobConfigure {

    private Scheduler scheduler;
    private String cronExpression;

    public TransactionMsgJobConfigure(String cronExpression){
        this.cronExpression = cronExpression;
    }
    public void init(){
        try {
            SchedulerFactory schedulerfactory = new StdSchedulerFactory();
            scheduler = schedulerfactory.getScheduler();
            MethodInvokingJobDetailFactoryBean jobDetail = new MethodInvokingJobDetailFactoryBean();
            TransactionMsgJob transactionMsgJob = FactoryBuilder.factoryOf(TransactionMsgJob.class, TransactionMsgJob.class).getInstance();
            jobDetail.setTargetObject(transactionMsgJob);
            jobDetail.setTargetMethod("execute");
            jobDetail.setName("TransactionMsgJob");
            jobDetail.setConcurrent(false);
            jobDetail.afterPropertiesSet();
            CronTriggerFactoryBean cronTrigger = new CronTriggerFactoryBean();
            cronTrigger.setBeanName("TransactionMsgJobConfigure");
            cronTrigger.setCronExpression(this.cronExpression);
            cronTrigger.setJobDetail(jobDetail.getObject());
            cronTrigger.afterPropertiesSet();
            scheduler.scheduleJob(jobDetail.getObject(), cronTrigger.getObject());
            scheduler.start();

        } catch (Exception e) {
            throw new TransactionException("transactionMsgJobConfigure start fail");
        }
    }
}
