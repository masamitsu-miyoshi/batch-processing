# batch-processing

### Run all jobs

``` 
$ java -jar target/batch-processing-0.0.1.RELEASE.jar
```

### Run only specified jobs


#### Only job1

```
$ java -jar target/batch-processing-0.0.1.RELEASE.jar --spring.batch.job.names=job1
```

#### Only job2

```
$ java -jar target/batch-processing-0.0.1.RELEASE.jar --spring.batch.job.names=job2
```

#### job1 and job2

```
$ java -jar target/batch-processing-0.0.1.RELEASE.jar --spring.batch.job.names=job1,job2
```