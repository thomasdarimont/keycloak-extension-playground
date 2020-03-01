Keycloak Micro-Profile Metrics Example
---

Simple extension which demonstrates how to expose Wildfly and Keycloak specific metrics by leveraging the [microprofile-metrics support of Wildfly](https://github.com/wildfly/wildfly/blob/master/docs/src/main/asciidoc/_admin-guide/subsystem-configuration/MicroProfile_Metrics.adoc).
This example is inspired by [aerogear/keycloak-metrics-spi](https://github.com/aerogear/keycloak-metrics-spi) but uses the API from
[eclipse/microprofile-metrics](https://github.com/eclipse/microprofile-metrics) backed by [smallrye-metrics](https://github.com/smallrye/smallrye-metrics).

# Build
Just build the jar and copy it into the `$KEYCLOAK_HOME/standalone/deployments` folder.
```
mvn clean package
``` 

# Run
To enable the smallrye-metrics support, you need to enable statistics collection, e.g.:  
`-Dwildfly.statistics-enabled=true`. This will expose the smallrye metrics via the 
`/metrics` endpoint on the Wildfly management interface, e.g. `http://localhost:9990`. 
```
bin/standalone.sh -Dwildfly.statistics-enabled=true
```
Note that the [Management Model Section of the microprofile-metrics Documentation] explains options for securing the `/metrics` endpoint.

# Access the Metrics

To access Metrics in text-based [Open Metrics Format](https://openmetrics.io/) simply browse to `http://localhost:9990/metrics`

# Metrics Example
```
# HELP application_keycloak_logins_total Total successful logins
# TYPE application_keycloak_logins_total counter
application_keycloak_logins_total{client_id="account",provider="keycloak",realm="demo-smallrye-metrics"} 2.0
# HELP application_keycloak_startup_total Keycloak Startup Counter
# TYPE application_keycloak_startup_total counter
application_keycloak_startup_total{type="ABC"} 1.0
# HELP application_keycloak_admin_event_UPDATE_total Generic KeyCloak Admin event
# TYPE application_keycloak_admin_event_UPDATE_total counter
application_keycloak_admin_event_UPDATE_total{realm="demo-smallrye-metrics",resource="REALM"} 1.0
# HELP application_keycloak_failed_login_attempts_total Total failed login attempts
# TYPE application_keycloak_failed_login_attempts_total counter
application_keycloak_failed_login_attempts_total{client_id="account",error="invalid_user_credentials",provider="keycloak",realm="demo-smallrye-metrics"} 2.0
# HELP application_keycloak_user_event_LOGOUT_total Generic KeyCloak User event
# TYPE application_keycloak_user_event_LOGOUT_total counter
application_keycloak_user_event_LOGOUT_total{realm="demo-smallrye-metrics"} 1.0
# HELP base_cpu_processCpuLoad Displays the "recent cpu usage" for the Java Virtual Machine process.
# TYPE base_cpu_processCpuLoad gauge
base_cpu_processCpuLoad 2.9712133492980548E-5
# HELP base_memory_committedNonHeap_bytes Displays the amount of memory that is committed for the Java virtual machine to use.
# TYPE base_memory_committedNonHeap_bytes gauge
base_memory_committedNonHeap_bytes 2.42352128E8
# HELP base_memory_maxHeap_bytes Displays the maximum amount of memory in bytes that can be used for memory management.
# TYPE base_memory_maxHeap_bytes gauge
base_memory_maxHeap_bytes 5.36870912E8
# HELP base_gc_time_total Displays the approximate accumulated collection elapsed time in milliseconds. This attribute displays -1 if the collection elapsed time is undefined for this collector. The Java virtual machine implementation may use a high resolution timer to measure the elapsed time. This attribute may display the same value even if the collection count has been incremented if the collection elapsed time is very short.
# TYPE base_gc_time_total counter
base_gc_time_total_seconds{name="G1 Young Generation1"} 0.303
# HELP base_gc_total Displays the total number of collections that have occurred. This attribute lists -1 if the collection count is undefined for this collector.
# TYPE base_gc_total counter
base_gc_total{name="G1 Young Generation1"} 84.0
# HELP base_gc_total Displays the total number of collections that have occurred. This attribute lists -1 if the collection count is undefined for this collector.
# TYPE base_gc_total counter
base_gc_total{name="G1 Old Generation1"} 0.0
# HELP base_gc_time_total Displays the approximate accumulated collection elapsed time in milliseconds. This attribute displays -1 if the collection elapsed time is undefined for this collector. The Java virtual machine implementation may use a high resolution timer to measure the elapsed time. This attribute may display the same value even if the collection count has been incremented if the collection elapsed time is very short.
# TYPE base_gc_time_total counter
base_gc_time_total_seconds{name="G1 Old Generation1"} 0.0
# HELP base_cpu_systemLoadAverage Displays the system load average for the last minute. The system load average is the sum of the number of runnable entities queued to the available processors and the number of runnable entities running on the available processors averaged over a period of time. The way in which the load average is calculated is operating system specific but is typically a damped time-dependent average. If the load average is not available, a negative value is displayed. This attribute is designed to provide a hint about the system load and may be queried frequently. The load average may be unavailable on some platform where it is expensive to implement this method.
# TYPE base_cpu_systemLoadAverage gauge
base_cpu_systemLoadAverage 1.33
# HELP base_memory_committedHeap_bytes Displays the amount of memory that is committed for the Java virtual machine to use.
# TYPE base_memory_committedHeap_bytes gauge
base_memory_committedHeap_bytes 1.16391936E8
# HELP base_memory_maxNonHeap_bytes Displays the maximum amount of memory in bytes that can be used for memory management.
# TYPE base_memory_maxNonHeap_bytes gauge
base_memory_maxNonHeap_bytes 7.80140544E8
# HELP base_thread_daemon_count Displays the current number of live daemon threads.
# TYPE base_thread_daemon_count gauge
base_thread_daemon_count 26.0
# HELP base_cpu_availableProcessors Displays the number of processors available to the Java virtual machine. This value may change during a particular invocation of the virtual machine.
# TYPE base_cpu_availableProcessors gauge
base_cpu_availableProcessors 12.0
# HELP base_memory_usedHeap_bytes Displays the amount of used memory.
# TYPE base_memory_usedHeap_bytes gauge
base_memory_usedHeap_bytes 9.7518416E7
# HELP base_thread_max_count Displays the peak live thread count since the Java virtual machine started or peak was reset. This includes daemon and non-daemon threads.
# TYPE base_thread_max_count gauge
base_thread_max_count 145.0
# HELP base_classloader_loadedClasses_count Displays the number of classes that are currently loaded in the Java virtual machine.
# TYPE base_classloader_loadedClasses_count gauge
base_classloader_loadedClasses_count 26708.0
# HELP base_thread_count Number of currently deployed threads
# TYPE base_thread_count gauge
base_thread_count 79.0
# HELP base_classloader_loadedClasses_total Displays the total number of classes that have been loaded since the Java virtual machine has started execution.
# TYPE base_classloader_loadedClasses_total counter
base_classloader_loadedClasses_total 26848.0
# HELP base_classloader_unloadedClasses_total Displays the total number of classes unloaded since the Java virtual machine has started execution.
# TYPE base_classloader_unloadedClasses_total counter
base_classloader_unloadedClasses_total 140.0
# HELP base_jvm_uptime_seconds Displays the uptime of the Java virtual machine
# TYPE base_jvm_uptime_seconds gauge
base_jvm_uptime_seconds 65.238
# HELP base_memory_usedNonHeap_bytes Displays the amount of used memory.
# TYPE base_memory_usedNonHeap_bytes gauge
base_memory_usedNonHeap_bytes 2.24485568E8
# HELP wildfly_datasources_pool_timed_out The timed out count
# TYPE wildfly_datasources_pool_timed_out gauge
wildfly_datasources_pool_timed_out{data_source="KeycloakDS",microprofile_scope="vendor"} 0.0
# HELP wildfly_undertow_max_processing_time_seconds The maximum processing time taken by a request on this listener
# TYPE wildfly_undertow_max_processing_time_seconds gauge
wildfly_undertow_max_processing_time_seconds{https_listener="https",server="default-server",microprofile_scope="vendor"} 0.0
# HELP wildfly_undertow_active_sessions Number of active sessions
# TYPE wildfly_undertow_active_sessions gauge
wildfly_undertow_active_sessions{deployment="keycloak-server.war",subdeployment="keycloak-server.war",microprofile_scope="vendor"} 0.0
# HELP wildfly_ejb3_current_thread_count The current number of threads in the pool.
# TYPE wildfly_ejb3_current_thread_count gauge
wildfly_ejb3_current_thread_count{thread_pool="default",microprofile_scope="vendor"} 0.0
# HELP vendor_BufferPool_used_memory_direct_bytes The memory used by the NIO pool: direct
# TYPE vendor_BufferPool_used_memory_direct_bytes gauge
vendor_BufferPool_used_memory_direct_bytes 551976.0
# HELP wildfly_jca_current_thread_count The current number of threads in the pool.
# TYPE wildfly_jca_current_thread_count gauge
wildfly_jca_current_thread_count{short_running_threads="default",workmanager="default",microprofile_scope="vendor"} 0.0
# HELP wildfly_datasources_jdbc_prepared_statement_cache_hit_count The number of times that statements from the cache were used
# TYPE wildfly_datasources_jdbc_prepared_statement_cache_hit_count gauge
wildfly_datasources_jdbc_prepared_statement_cache_hit_count{data_source="KeycloakDS",microprofile_scope="vendor"} 0.0
# HELP wildfly_datasources_pool_xaend_max_time The maximum time for a XAResource end invocation
# TYPE wildfly_datasources_pool_xaend_max_time gauge
wildfly_datasources_pool_xaend_max_time{data_source="ExampleDS",microprofile_scope="vendor"} 0.0
# HELP wildfly_transactions_number_of_committed_transactions_total The number of committed transactions.
# TYPE wildfly_transactions_number_of_committed_transactions_total counter
wildfly_transactions_number_of_committed_transactions_total{microprofile_scope="vendor"} 65.0
# HELP vendor_memoryPool_Metaspace_usage_bytes Current usage of the Metaspace memory pool
# TYPE vendor_memoryPool_Metaspace_usage_bytes gauge
vendor_memoryPool_Metaspace_usage_bytes 1.56365936E8
# HELP wildfly_datasources_pool_total_get_time The total time spent obtaining physical connections
# TYPE wildfly_datasources_pool_total_get_time gauge
wildfly_datasources_pool_total_get_time{data_source="ExampleDS",microprofile_scope="vendor"} 0.0
# HELP wildfly_jca_local_work_failed Number of works failed
# TYPE wildfly_jca_local_work_failed gauge
wildfly_jca_local_work_failed{workmanager="default",microprofile_scope="vendor"} 0.0
# HELP vendor_memoryPool_G1_Survivor_Space_usage_max_bytes Peak usage of the G1 Survivor Space memory pool
# TYPE vendor_memoryPool_G1_Survivor_Space_usage_max_bytes gauge
vendor_memoryPool_G1_Survivor_Space_usage_max_bytes 5242880.0
# HELP wildfly_datasources_pool_active_count The active count
# TYPE wildfly_datasources_pool_active_count gauge
wildfly_datasources_pool_active_count{data_source="KeycloakDS",microprofile_scope="vendor"} 3.0
# HELP wildfly_undertow_error_count_total The number of 500 responses that have been sent by this listener
# TYPE wildfly_undertow_error_count_total counter
wildfly_undertow_error_count_total{http_listener="default",server="default-server",microprofile_scope="vendor"} 0.0
# HELP wildfly_jca_rejected_count The number of tasks that have been passed to the handoff-executor (if one is specified) or discarded.
# TYPE wildfly_jca_rejected_count gauge
wildfly_jca_rejected_count{short_running_threads="default",workmanager="default",microprofile_scope="vendor"} 0.0
wildfly_datasources_pool_active_count{data_source="ExampleDS",microprofile_scope="vendor"} 0.0
# HELP wildfly_undertow_request_count_total Number of all requests
# TYPE wildfly_undertow_request_count_total counter
wildfly_undertow_request_count_total{deployment="keycloak-server.war",servlet="Keycloak REST Interface",subdeployment="keycloak-server.war",microprofile_scope="vendor"} 44.0
# HELP wildfly_ejb3_largest_thread_count The largest number of threads that have ever simultaneously been in the pool.
# TYPE wildfly_ejb3_largest_thread_count gauge
wildfly_ejb3_largest_thread_count{thread_pool="default",microprofile_scope="vendor"} 0.0
# HELP wildfly_datasources_pool_xaend_count The number of XAResource end invocations
# TYPE wildfly_datasources_pool_xaend_count gauge
wildfly_datasources_pool_xaend_count{data_source="KeycloakDS",microprofile_scope="vendor"} 0.0
# HELP vendor_memoryPool_CodeHeap_non_nmethods_usage_max_bytes Peak usage of the CodeHeap 'non-nmethods' memory pool
# TYPE vendor_memoryPool_CodeHeap_non_nmethods_usage_max_bytes gauge
vendor_memoryPool_CodeHeap_non_nmethods_usage_max_bytes 1541760.0
# HELP wildfly_datasources_pool_average_usage_time The average time spent using a physical connection
# TYPE wildfly_datasources_pool_average_usage_time gauge
wildfly_datasources_pool_average_usage_time{data_source="ExampleDS",microprofile_scope="vendor"} 0.0
wildfly_datasources_pool_average_usage_time{data_source="KeycloakDS",microprofile_scope="vendor"} 347.0
# HELP wildfly_datasources_pool_xaprepare_count The number of XAResource prepare invocations
# TYPE wildfly_datasources_pool_xaprepare_count gauge
wildfly_datasources_pool_xaprepare_count{data_source="KeycloakDS",microprofile_scope="vendor"} 0.0
wildfly_datasources_jdbc_prepared_statement_cache_hit_count{data_source="ExampleDS",microprofile_scope="vendor"} 0.0
# HELP wildfly_ejb3_task_count The approximate total number of tasks that have ever been scheduled for execution.
# TYPE wildfly_ejb3_task_count gauge
wildfly_ejb3_task_count{thread_pool="default",microprofile_scope="vendor"} 0.0
wildfly_datasources_pool_xaend_max_time{data_source="KeycloakDS",microprofile_scope="vendor"} 0.0
wildfly_datasources_pool_xaprepare_count{data_source="ExampleDS",microprofile_scope="vendor"} 0.0
# HELP wildfly_transactions_number_of_aborted_transactions_total The number of aborted (i.e. rolledback) transactions.
# TYPE wildfly_transactions_number_of_aborted_transactions_total counter
wildfly_transactions_number_of_aborted_transactions_total{microprofile_scope="vendor"} 0.0
# HELP wildfly_datasources_pool_xacommit_total_time The total time for all XAResource commit invocations
# TYPE wildfly_datasources_pool_xacommit_total_time gauge
wildfly_datasources_pool_xacommit_total_time{data_source="KeycloakDS",microprofile_scope="vendor"} 4.0
# HELP wildfly_datasources_pool_total_pool_time The total time spent by physical connections in the pool
# TYPE wildfly_datasources_pool_total_pool_time gauge
wildfly_datasources_pool_total_pool_time{data_source="ExampleDS",microprofile_scope="vendor"} 0.0
wildfly_datasources_pool_total_pool_time{data_source="KeycloakDS",microprofile_scope="vendor"} 45101.0
# HELP wildfly_datasources_pool_max_get_time The maximum time for obtaining a physical connection
# TYPE wildfly_datasources_pool_max_get_time gauge
wildfly_datasources_pool_max_get_time{data_source="KeycloakDS",microprofile_scope="vendor"} 233.0
# HELP wildfly_datasources_pool_xacommit_average_time The average time for a XAResource commit invocation
# TYPE wildfly_datasources_pool_xacommit_average_time gauge
wildfly_datasources_pool_xacommit_average_time{data_source="KeycloakDS",microprofile_scope="vendor"} 0.0
# HELP wildfly_jca_queue_size The queue size.
# TYPE wildfly_jca_queue_size gauge
wildfly_jca_queue_size{short_running_threads="default",workmanager="default",microprofile_scope="vendor"} 0.0
wildfly_datasources_pool_xacommit_average_time{data_source="ExampleDS",microprofile_scope="vendor"} 0.0
# HELP vendor_memoryPool_G1_Old_Gen_usage_bytes Current usage of the G1 Old Gen memory pool
# TYPE vendor_memoryPool_G1_Old_Gen_usage_bytes gauge
vendor_memoryPool_G1_Old_Gen_usage_bytes 8.7032656E7
# HELP wildfly_jca_largest_thread_count The largest number of threads that have ever simultaneously been in the pool.
# TYPE wildfly_jca_largest_thread_count gauge
wildfly_jca_largest_thread_count{short_running_threads="default",workmanager="default",microprofile_scope="vendor"} 0.0
# HELP wildfly_io_io_thread_count I/O thread count
# TYPE wildfly_io_io_thread_count gauge
wildfly_io_io_thread_count{worker="default",microprofile_scope="vendor"} 24.0
wildfly_datasources_pool_xaend_count{data_source="ExampleDS",microprofile_scope="vendor"} 0.0
# HELP wildfly_transactions_number_of_nested_transactions_total The total number of nested (sub) transactions created.
# TYPE wildfly_transactions_number_of_nested_transactions_total counter
wildfly_transactions_number_of_nested_transactions_total{microprofile_scope="vendor"} 0.0
# HELP wildfly_datasources_jdbc_prepared_statement_cache_current_size The number of prepared and callable statements currently cached in the statement cache
# TYPE wildfly_datasources_jdbc_prepared_statement_cache_current_size gauge
wildfly_datasources_jdbc_prepared_statement_cache_current_size{data_source="KeycloakDS",microprofile_scope="vendor"} 0.0
# HELP wildfly_datasources_pool_available_count The available count
# TYPE wildfly_datasources_pool_available_count gauge
wildfly_datasources_pool_available_count{data_source="KeycloakDS",microprofile_scope="vendor"} 20.0
# HELP wildfly_datasources_pool_xarecover_total_time The total time for all XAResource recover invocations
# TYPE wildfly_datasources_pool_xarecover_total_time gauge
wildfly_datasources_pool_xarecover_total_time{data_source="ExampleDS",microprofile_scope="vendor"} 0.0
wildfly_datasources_pool_xarecover_total_time{data_source="KeycloakDS",microprofile_scope="vendor"} 0.0
# HELP vendor_memoryPool_CodeHeap_non_profiled_nmethods_usage_bytes Current usage of the CodeHeap 'non-profiled nmethods' memory pool
# TYPE vendor_memoryPool_CodeHeap_non_profiled_nmethods_usage_bytes gauge
vendor_memoryPool_CodeHeap_non_profiled_nmethods_usage_bytes 1.137472E7
wildfly_datasources_jdbc_prepared_statement_cache_current_size{data_source="ExampleDS",microprofile_scope="vendor"} 0.0
# HELP wildfly_transactions_average_commit_time_seconds The average time of transaction commit, measured from the moment the client calls commit until the transaction manager determines that the commit attempt was successful.
# TYPE wildfly_transactions_average_commit_time_seconds gauge
wildfly_transactions_average_commit_time_seconds{microprofile_scope="vendor"} 6.7758E-5
wildfly_datasources_pool_available_count{data_source="ExampleDS",microprofile_scope="vendor"} 20.0
wildfly_datasources_pool_max_get_time{data_source="ExampleDS",microprofile_scope="vendor"} 0.0
# HELP wildfly_undertow_min_request_time_seconds Minimal time for processing request
# TYPE wildfly_undertow_min_request_time_seconds gauge
wildfly_undertow_min_request_time_seconds{deployment="keycloak-server.war",servlet="Keycloak REST Interface",subdeployment="keycloak-server.war",microprofile_scope="vendor"} 0.003
# HELP wildfly_datasources_pool_xastart_count The number of XAResource start invocations
# TYPE wildfly_datasources_pool_xastart_count gauge
wildfly_datasources_pool_xastart_count{data_source="ExampleDS",microprofile_scope="vendor"} 0.0
# HELP wildfly_transactions_number_of_system_rollbacks_total The number of transactions that have been rolled back due to internal system errors.
# TYPE wildfly_transactions_number_of_system_rollbacks_total counter
wildfly_transactions_number_of_system_rollbacks_total{microprofile_scope="vendor"} 0.0
# HELP wildfly_io_max_pool_size The maximum number of threads to allow in the thread pool. Depending on implementation, when this limit is reached, tasks which cannot be queued may be rejected.
# TYPE wildfly_io_max_pool_size gauge
wildfly_io_max_pool_size{worker="default",microprofile_scope="vendor"} 192.0
# HELP wildfly_io_queue_size An estimate of the number of tasks in the worker queue.
# TYPE wildfly_io_queue_size gauge
wildfly_io_queue_size{worker="default",microprofile_scope="vendor"} 0.0
wildfly_datasources_pool_xastart_count{data_source="KeycloakDS",microprofile_scope="vendor"} 27.0
# HELP wildfly_datasources_pool_xacommit_max_time The maximum time for a XAResource commit invocation
# TYPE wildfly_datasources_pool_xacommit_max_time gauge
wildfly_datasources_pool_xacommit_max_time{data_source="ExampleDS",microprofile_scope="vendor"} 0.0
# HELP wildfly_datasources_pool_average_blocking_time Average Blocking Time for pool
# TYPE wildfly_datasources_pool_average_blocking_time gauge
wildfly_datasources_pool_average_blocking_time{data_source="KeycloakDS",microprofile_scope="vendor"} 0.0
# HELP wildfly_datasources_pool_xarollback_count The number of XAResource rollback invocations
# TYPE wildfly_datasources_pool_xarollback_count gauge
wildfly_datasources_pool_xarollback_count{data_source="ExampleDS",microprofile_scope="vendor"} 0.0
# HELP wildfly_datasources_pool_blocking_failure_count The number of failures trying to obtain a physical connection
# TYPE wildfly_datasources_pool_blocking_failure_count gauge
wildfly_datasources_pool_blocking_failure_count{data_source="ExampleDS",microprofile_scope="vendor"} 0.0
# HELP wildfly_datasources_pool_idle_count The number of physical connections currently idle
# TYPE wildfly_datasources_pool_idle_count gauge
wildfly_datasources_pool_idle_count{data_source="ExampleDS",microprofile_scope="vendor"} 0.0
# HELP wildfly_undertow_processing_time_total The total processing time of all requests handed by this listener
# TYPE wildfly_undertow_processing_time_total counter
wildfly_undertow_processing_time_total_seconds{http_listener="default",server="default-server",microprofile_scope="vendor"} 0.0
# HELP wildfly_datasources_pool_xacommit_count The number of XAResource commit invocations
# TYPE wildfly_datasources_pool_xacommit_count gauge
wildfly_datasources_pool_xacommit_count{data_source="ExampleDS",microprofile_scope="vendor"} 0.0
wildfly_datasources_pool_average_blocking_time{data_source="ExampleDS",microprofile_scope="vendor"} 0.0
# HELP wildfly_datasources_pool_xarecover_average_time The average time for a XAResource recover invocation
# TYPE wildfly_datasources_pool_xarecover_average_time gauge
wildfly_datasources_pool_xarecover_average_time{data_source="KeycloakDS",microprofile_scope="vendor"} 0.0
wildfly_jca_queue_size{long_running_threads="default",workmanager="default",microprofile_scope="vendor"} 0.0
# HELP wildfly_io_core_pool_size Minimum number of threads to keep in the underlying thread pool even if they are idle. Threads over this limit will be terminated over time specified by task-keepalive attribute.
# TYPE wildfly_io_core_pool_size gauge
wildfly_io_core_pool_size{worker="default",microprofile_scope="vendor"} 2.0
# HELP wildfly_transactions_number_of_heuristics_total The number of transactions which have terminated with heuristic outcomes.
# TYPE wildfly_transactions_number_of_heuristics_total counter
wildfly_transactions_number_of_heuristics_total{microprofile_scope="vendor"} 0.0
# HELP wildfly_undertow_bytes_sent_total The number of bytes that have been sent out on this listener
# TYPE wildfly_undertow_bytes_sent_total counter
wildfly_undertow_bytes_sent_total_bytes{https_listener="https",server="default-server",microprofile_scope="vendor"} 0.0
# HELP wildfly_datasources_pool_xarollback_max_time The maximum time for a XAResource rollback invocation
# TYPE wildfly_datasources_pool_xarollback_max_time gauge
wildfly_datasources_pool_xarollback_max_time{data_source="ExampleDS",microprofile_scope="vendor"} 0.0
# HELP wildfly_datasources_pool_wait_count The number of requests that had to wait to obtain a physical connection
# TYPE wildfly_datasources_pool_wait_count gauge
wildfly_datasources_pool_wait_count{data_source="ExampleDS",microprofile_scope="vendor"} 0.0
wildfly_datasources_pool_xarecover_average_time{data_source="ExampleDS",microprofile_scope="vendor"} 0.0
# HELP wildfly_request_controller_active_requests The number of requests that are currently running in the server
# TYPE wildfly_request_controller_active_requests gauge
wildfly_request_controller_active_requests{microprofile_scope="vendor"} 0.0
wildfly_undertow_max_processing_time_seconds{http_listener="default",server="default-server",microprofile_scope="vendor"} 0.0
wildfly_datasources_pool_xacommit_count{data_source="KeycloakDS",microprofile_scope="vendor"} 27.0
wildfly_datasources_pool_xarollback_max_time{data_source="KeycloakDS",microprofile_scope="vendor"} 0.0
wildfly_datasources_pool_xacommit_max_time{data_source="KeycloakDS",microprofile_scope="vendor"} 3.0
wildfly_datasources_pool_xarollback_count{data_source="KeycloakDS",microprofile_scope="vendor"} 0.0
# HELP vendor_memoryPool_Compressed_Class_Space_usage_bytes Current usage of the Compressed Class Space memory pool
# TYPE vendor_memoryPool_Compressed_Class_Space_usage_bytes gauge
vendor_memoryPool_Compressed_Class_Space_usage_bytes 1.9631488E7
wildfly_datasources_pool_wait_count{data_source="KeycloakDS",microprofile_scope="vendor"} 0.0
# HELP wildfly_ejb3_queue_size The queue size.
# TYPE wildfly_ejb3_queue_size gauge
wildfly_ejb3_queue_size{thread_pool="default",microprofile_scope="vendor"} 0.0
# HELP wildfly_ejb3_active_count The approximate number of threads that are actively executing tasks.
# TYPE wildfly_ejb3_active_count gauge
wildfly_ejb3_active_count{thread_pool="default",microprofile_scope="vendor"} 0.0
# HELP wildfly_datasources_jdbc_prepared_statement_cache_miss_count The number of times that a statement request could not be satisfied with a statement from the cache
# TYPE wildfly_datasources_jdbc_prepared_statement_cache_miss_count gauge
wildfly_datasources_jdbc_prepared_statement_cache_miss_count{data_source="KeycloakDS",microprofile_scope="vendor"} 0.0
wildfly_datasources_jdbc_prepared_statement_cache_miss_count{data_source="ExampleDS",microprofile_scope="vendor"} 0.0
# HELP vendor_memoryPool_CodeHeap_profiled_nmethods_usage_max_bytes Peak usage of the CodeHeap 'profiled nmethods' memory pool
# TYPE vendor_memoryPool_CodeHeap_profiled_nmethods_usage_max_bytes gauge
vendor_memoryPool_CodeHeap_profiled_nmethods_usage_max_bytes 3.5796352E7
wildfly_undertow_request_count_total{https_listener="https",server="default-server",microprofile_scope="vendor"} 0.0
# HELP wildfly_datasources_pool_average_creation_time The average time spent creating a physical connection
# TYPE wildfly_datasources_pool_average_creation_time gauge
wildfly_datasources_pool_average_creation_time{data_source="KeycloakDS",microprofile_scope="vendor"} 76.0
# HELP wildfly_datasources_pool_xaprepare_max_time The maximum time for a XAResource prepare invocation
# TYPE wildfly_datasources_pool_xaprepare_max_time gauge
wildfly_datasources_pool_xaprepare_max_time{data_source="KeycloakDS",microprofile_scope="vendor"} 0.0
# HELP vendor_memoryPool_Compressed_Class_Space_usage_max_bytes Peak usage of the Compressed Class Space memory pool
# TYPE vendor_memoryPool_Compressed_Class_Space_usage_max_bytes gauge
vendor_memoryPool_Compressed_Class_Space_usage_max_bytes 1.9631488E7
wildfly_datasources_pool_xaprepare_max_time{data_source="ExampleDS",microprofile_scope="vendor"} 0.0
# HELP wildfly_datasources_jdbc_prepared_statement_cache_access_count The number of times that the statement cache was accessed
# TYPE wildfly_datasources_jdbc_prepared_statement_cache_access_count gauge
wildfly_datasources_jdbc_prepared_statement_cache_access_count{data_source="ExampleDS",microprofile_scope="vendor"} 0.0
# HELP wildfly_undertow_total_request_time_total Total time spend in processing all requests
# TYPE wildfly_undertow_total_request_time_total counter
wildfly_undertow_total_request_time_total_seconds{deployment="keycloak-server.war",servlet="Keycloak REST Interface",subdeployment="keycloak-server.war",microprofile_scope="vendor"} 3.455
wildfly_datasources_jdbc_prepared_statement_cache_access_count{data_source="KeycloakDS",microprofile_scope="vendor"} 0.0
# HELP wildfly_datasources_pool_created_count The created count
# TYPE wildfly_datasources_pool_created_count gauge
wildfly_datasources_pool_created_count{data_source="KeycloakDS",microprofile_scope="vendor"} 3.0
# HELP wildfly_transactions_number_of_application_rollbacks_total The number of transactions that have been rolled back by application request. This includes those that timeout, since the timeout behavior is considered an attribute of the application configuration.
# TYPE wildfly_transactions_number_of_application_rollbacks_total counter
wildfly_transactions_number_of_application_rollbacks_total{microprofile_scope="vendor"} 0.0
wildfly_datasources_pool_idle_count{data_source="KeycloakDS",microprofile_scope="vendor"} 3.0
wildfly_jca_rejected_count{long_running_threads="default",workmanager="default",microprofile_scope="vendor"} 0.0
wildfly_datasources_pool_average_creation_time{data_source="ExampleDS",microprofile_scope="vendor"} 0.0
wildfly_undertow_error_count_total{https_listener="https",server="default-server",microprofile_scope="vendor"} 0.0
wildfly_datasources_pool_created_count{data_source="ExampleDS",microprofile_scope="vendor"} 0.0
# HELP wildfly_undertow_session_avg_alive_time_seconds Average time that expired sessions had been alive
# TYPE wildfly_undertow_session_avg_alive_time_seconds gauge
wildfly_undertow_session_avg_alive_time_seconds{deployment="keycloak-server.war",subdeployment="keycloak-server.war",microprofile_scope="vendor"} 0.0
# HELP wildfly_datasources_pool_xaforget_total_time The total time for all XAResource forget invocations
# TYPE wildfly_datasources_pool_xaforget_total_time gauge
wildfly_datasources_pool_xaforget_total_time{data_source="KeycloakDS",microprofile_scope="vendor"} 0.0
wildfly_datasources_pool_xaforget_total_time{data_source="ExampleDS",microprofile_scope="vendor"} 0.0
# HELP wildfly_undertow_rejected_sessions_total Number of rejected sessions
# TYPE wildfly_undertow_rejected_sessions_total counter
wildfly_undertow_rejected_sessions_total{deployment="keycloak-server.war",subdeployment="keycloak-server.war",microprofile_scope="vendor"} 0.0
wildfly_jca_largest_thread_count{long_running_threads="default",workmanager="default",microprofile_scope="vendor"} 0.0
# HELP wildfly_transactions_number_of_resource_rollbacks_total The number of transactions that rolled back due to resource (participant) failure.
# TYPE wildfly_transactions_number_of_resource_rollbacks_total counter
wildfly_transactions_number_of_resource_rollbacks_total{microprofile_scope="vendor"} 0.0
# HELP wildfly_datasources_pool_max_usage_time The maximum time using a physical connection
# TYPE wildfly_datasources_pool_max_usage_time gauge
wildfly_datasources_pool_max_usage_time{data_source="KeycloakDS",microprofile_scope="vendor"} 4342.0
# HELP wildfly_datasources_jdbc_prepared_statement_cache_delete_count The number of statements discarded from the cache
# TYPE wildfly_datasources_jdbc_prepared_statement_cache_delete_count gauge
wildfly_datasources_jdbc_prepared_statement_cache_delete_count{data_source="ExampleDS",microprofile_scope="vendor"} 0.0
# HELP wildfly_datasources_pool_total_blocking_time The total blocking time
# TYPE wildfly_datasources_pool_total_blocking_time gauge
wildfly_datasources_pool_total_blocking_time{data_source="KeycloakDS",microprofile_scope="vendor"} 0.0
# HELP wildfly_undertow_max_request_time_seconds Maximal time for processing request
# TYPE wildfly_undertow_max_request_time_seconds gauge
wildfly_undertow_max_request_time_seconds{deployment="keycloak-server.war",servlet="Keycloak REST Interface",subdeployment="keycloak-server.war",microprofile_scope="vendor"} 0.496
# HELP wildfly_datasources_pool_max_pool_time The maximum time for a physical connection in the pool
# TYPE wildfly_datasources_pool_max_pool_time gauge
wildfly_datasources_pool_max_pool_time{data_source="ExampleDS",microprofile_scope="vendor"} 0.0
wildfly_datasources_pool_max_pool_time{data_source="KeycloakDS",microprofile_scope="vendor"} 7154.0
# HELP wildfly_datasources_pool_xaforget_count The number of XAResource forget invocations
# TYPE wildfly_datasources_pool_xaforget_count gauge
wildfly_datasources_pool_xaforget_count{data_source="ExampleDS",microprofile_scope="vendor"} 0.0
# HELP wildfly_datasources_pool_xastart_average_time The average time for a XAResource start invocation
# TYPE wildfly_datasources_pool_xastart_average_time gauge
wildfly_datasources_pool_xastart_average_time{data_source="ExampleDS",microprofile_scope="vendor"} 0.0
# HELP vendor_memoryPool_CodeHeap_profiled_nmethods_usage_bytes Current usage of the CodeHeap 'profiled nmethods' memory pool
# TYPE vendor_memoryPool_CodeHeap_profiled_nmethods_usage_bytes gauge
vendor_memoryPool_CodeHeap_profiled_nmethods_usage_bytes 3.5851904E7
wildfly_datasources_pool_total_blocking_time{data_source="ExampleDS",microprofile_scope="vendor"} 0.0
wildfly_datasources_pool_xastart_average_time{data_source="KeycloakDS",microprofile_scope="vendor"} 0.0
# HELP wildfly_transactions_number_of_timed_out_transactions_total The number of transactions that have rolled back due to timeout.
# TYPE wildfly_transactions_number_of_timed_out_transactions_total counter
wildfly_transactions_number_of_timed_out_transactions_total{microprofile_scope="vendor"} 0.0
# HELP wildfly_ejb3_completed_task_count The approximate total number of tasks that have completed execution.
# TYPE wildfly_ejb3_completed_task_count gauge
wildfly_ejb3_completed_task_count{thread_pool="default",microprofile_scope="vendor"} 0.0
# HELP wildfly_datasources_pool_xarecover_max_time The maximum time for a XAResource recover invocation
# TYPE wildfly_datasources_pool_xarecover_max_time gauge
wildfly_datasources_pool_xarecover_max_time{data_source="KeycloakDS",microprofile_scope="vendor"} 0.0
# HELP wildfly_datasources_pool_max_wait_count The maximum number of threads waiting for a connection
# TYPE wildfly_datasources_pool_max_wait_count gauge
wildfly_datasources_pool_max_wait_count{data_source="ExampleDS",microprofile_scope="vendor"} 0.0
wildfly_jca_current_thread_count{long_running_threads="default",workmanager="default",microprofile_scope="vendor"} 0.0
wildfly_datasources_pool_max_usage_time{data_source="ExampleDS",microprofile_scope="vendor"} 0.0
# HELP wildfly_datasources_pool_in_use_count The number of physical connections currently in use
# TYPE wildfly_datasources_pool_in_use_count gauge
wildfly_datasources_pool_in_use_count{data_source="KeycloakDS",microprofile_scope="vendor"} 0.0
# HELP wildfly_datasources_pool_xaend_total_time The total time for all XAResource end invocations
# TYPE wildfly_datasources_pool_xaend_total_time gauge
wildfly_datasources_pool_xaend_total_time{data_source="KeycloakDS",microprofile_scope="vendor"} 0.0
# HELP wildfly_jca_local_work_active Number of current active works
# TYPE wildfly_jca_local_work_active gauge
wildfly_jca_local_work_active{workmanager="default",microprofile_scope="vendor"} 0.0
wildfly_datasources_jdbc_prepared_statement_cache_delete_count{data_source="KeycloakDS",microprofile_scope="vendor"} 0.0
# HELP wildfly_jca_local_work_successful Number of works completed successfully
# TYPE wildfly_jca_local_work_successful gauge
wildfly_jca_local_work_successful{workmanager="default",microprofile_scope="vendor"} 0.0
# HELP wildfly_datasources_pool_average_pool_time The average time for a physical connection spent in the pool
# TYPE wildfly_datasources_pool_average_pool_time gauge
wildfly_datasources_pool_average_pool_time{data_source="ExampleDS",microprofile_scope="vendor"} 0.0
wildfly_datasources_pool_average_pool_time{data_source="KeycloakDS",microprofile_scope="vendor"} 1610.0
# HELP vendor_memoryPool_G1_Old_Gen_usage_max_bytes Peak usage of the G1 Old Gen memory pool
# TYPE vendor_memoryPool_G1_Old_Gen_usage_max_bytes gauge
vendor_memoryPool_G1_Old_Gen_usage_max_bytes 8.770924E7
# HELP wildfly_undertow_highest_session_count The maximum number of sessions that have been active simultaneously
# TYPE wildfly_undertow_highest_session_count gauge
wildfly_undertow_highest_session_count{deployment="keycloak-server.war",subdeployment="keycloak-server.war",microprofile_scope="vendor"} 0.0
wildfly_datasources_pool_in_use_count{data_source="ExampleDS",microprofile_scope="vendor"} 0.0
wildfly_datasources_pool_xaend_total_time{data_source="ExampleDS",microprofile_scope="vendor"} 0.0
# HELP wildfly_datasources_pool_max_used_count The maximum number of connections used
# TYPE wildfly_datasources_pool_max_used_count gauge
wildfly_datasources_pool_max_used_count{data_source="KeycloakDS",microprofile_scope="vendor"} 3.0
wildfly_datasources_pool_max_wait_count{data_source="KeycloakDS",microprofile_scope="vendor"} 0.0
# HELP wildfly_undertow_bytes_received_total The number of bytes that have been received by this listener
# TYPE wildfly_undertow_bytes_received_total counter
wildfly_undertow_bytes_received_total_bytes{http_listener="default",server="default-server",microprofile_scope="vendor"} 68245.0
# HELP vendor_memoryPool_Metaspace_usage_max_bytes Peak usage of the Metaspace memory pool
# TYPE vendor_memoryPool_Metaspace_usage_max_bytes gauge
vendor_memoryPool_Metaspace_usage_max_bytes 1.564408E8
# HELP wildfly_undertow_session_max_alive_time_seconds The longest time that an expired session had been alive
# TYPE wildfly_undertow_session_max_alive_time_seconds gauge
wildfly_undertow_session_max_alive_time_seconds{deployment="keycloak-server.war",subdeployment="keycloak-server.war",microprofile_scope="vendor"} 0.0
wildfly_datasources_pool_xarecover_max_time{data_source="ExampleDS",microprofile_scope="vendor"} 0.0
# HELP wildfly_transactions_number_of_transactions_total The total number of transactions (top-level and nested) created
# TYPE wildfly_transactions_number_of_transactions_total counter
wildfly_transactions_number_of_transactions_total{microprofile_scope="vendor"} 65.0
# HELP wildfly_jca_local_startwork_rejected Number of startWork calls rejected
# TYPE wildfly_jca_local_startwork_rejected gauge
wildfly_jca_local_startwork_rejected{workmanager="default",microprofile_scope="vendor"} 0.0
wildfly_datasources_pool_xaforget_count{data_source="KeycloakDS",microprofile_scope="vendor"} 0.0
# HELP wildfly_datasources_pool_total_creation_time The total time spent creating physical connections
# TYPE wildfly_datasources_pool_total_creation_time gauge
wildfly_datasources_pool_total_creation_time{data_source="KeycloakDS",microprofile_scope="vendor"} 229.0
# HELP wildfly_io_busy_task_thread_count An estimate of busy threads in the task worker thread pool
# TYPE wildfly_io_busy_task_thread_count gauge
wildfly_io_busy_task_thread_count{worker="default",microprofile_scope="vendor"} 0.0
# HELP wildfly_datasources_jdbc_prepared_statement_cache_add_count The number of statements added to the statement cache
# TYPE wildfly_datasources_jdbc_prepared_statement_cache_add_count gauge
wildfly_datasources_jdbc_prepared_statement_cache_add_count{data_source="KeycloakDS",microprofile_scope="vendor"} 0.0
# HELP wildfly_datasources_pool_average_get_time The average time spent obtaining a physical connection
# TYPE wildfly_datasources_pool_average_get_time gauge
wildfly_datasources_pool_average_get_time{data_source="KeycloakDS",microprofile_scope="vendor"} 59.0
wildfly_datasources_pool_average_get_time{data_source="ExampleDS",microprofile_scope="vendor"} 0.0
wildfly_datasources_pool_total_creation_time{data_source="ExampleDS",microprofile_scope="vendor"} 0.0
# HELP vendor_memoryPool_G1_Eden_Space_usage_max_bytes Peak usage of the G1 Eden Space memory pool
# TYPE vendor_memoryPool_G1_Eden_Space_usage_max_bytes gauge
vendor_memoryPool_G1_Eden_Space_usage_max_bytes 3.4603008E7
wildfly_datasources_jdbc_prepared_statement_cache_add_count{data_source="ExampleDS",microprofile_scope="vendor"} 0.0
wildfly_datasources_pool_blocking_failure_count{data_source="KeycloakDS",microprofile_scope="vendor"} 0.0
# HELP wildfly_datasources_pool_xastart_max_time The maximum time for a XAResource start invocation
# TYPE wildfly_datasources_pool_xastart_max_time gauge
wildfly_datasources_pool_xastart_max_time{data_source="ExampleDS",microprofile_scope="vendor"} 0.0
wildfly_datasources_pool_xastart_max_time{data_source="KeycloakDS",microprofile_scope="vendor"} 1.0
# HELP vendor_memoryPool_G1_Survivor_Space_usage_bytes Current usage of the G1 Survivor Space memory pool
# TYPE vendor_memoryPool_G1_Survivor_Space_usage_bytes gauge
vendor_memoryPool_G1_Survivor_Space_usage_bytes 2097152.0
# HELP wildfly_undertow_expired_sessions_total Number of sessions that have expired
# TYPE wildfly_undertow_expired_sessions_total counter
wildfly_undertow_expired_sessions_total{deployment="keycloak-server.war",subdeployment="keycloak-server.war",microprofile_scope="vendor"} 0.0
# HELP wildfly_transactions_number_of_inflight_transactions The number of transactions that have begun but not yet terminated.
# TYPE wildfly_transactions_number_of_inflight_transactions gauge
wildfly_transactions_number_of_inflight_transactions{microprofile_scope="vendor"} 0.0
# HELP wildfly_datasources_pool_max_wait_time The maximum wait time for a connection
# TYPE wildfly_datasources_pool_max_wait_time gauge
wildfly_datasources_pool_max_wait_time{data_source="KeycloakDS",microprofile_scope="vendor"} 0.0
# HELP wildfly_datasources_pool_xaforget_average_time The average time for a XAResource forget invocation
# TYPE wildfly_datasources_pool_xaforget_average_time gauge
wildfly_datasources_pool_xaforget_average_time{data_source="ExampleDS",microprofile_scope="vendor"} 0.0
# HELP vendor_memoryPool_G1_Eden_Space_usage_bytes Current usage of the G1 Eden Space memory pool
# TYPE vendor_memoryPool_G1_Eden_Space_usage_bytes gauge
vendor_memoryPool_G1_Eden_Space_usage_bytes 3145728.0
# HELP wildfly_datasources_pool_xaprepare_average_time The average time for a XAResource prepare invocation
# TYPE wildfly_datasources_pool_xaprepare_average_time gauge
wildfly_datasources_pool_xaprepare_average_time{data_source="ExampleDS",microprofile_scope="vendor"} 0.0
# HELP wildfly_datasources_pool_xaforget_max_time The maximum time for a XAResource forget invocation
# TYPE wildfly_datasources_pool_xaforget_max_time gauge
wildfly_datasources_pool_xaforget_max_time{data_source="KeycloakDS",microprofile_scope="vendor"} 0.0
# HELP vendor_memoryPool_CodeHeap_non_nmethods_usage_bytes Current usage of the CodeHeap 'non-nmethods' memory pool
# TYPE vendor_memoryPool_CodeHeap_non_nmethods_usage_bytes gauge
vendor_memoryPool_CodeHeap_non_nmethods_usage_bytes 1476736.0
# HELP wildfly_datasources_pool_xaend_average_time The average time for a XAResource end invocation
# TYPE wildfly_datasources_pool_xaend_average_time gauge
wildfly_datasources_pool_xaend_average_time{data_source="ExampleDS",microprofile_scope="vendor"} 0.0
# HELP wildfly_datasources_pool_xarollback_average_time The average time for a XAResource rollback invocation
# TYPE wildfly_datasources_pool_xarollback_average_time gauge
wildfly_datasources_pool_xarollback_average_time{data_source="ExampleDS",microprofile_scope="vendor"} 0.0
# HELP wildfly_ejb3_rejected_count The number of tasks that have been rejected.
# TYPE wildfly_ejb3_rejected_count gauge
wildfly_ejb3_rejected_count{thread_pool="default",microprofile_scope="vendor"} 0.0
# HELP wildfly_datasources_pool_xarecover_count The number of XAResource recover invocations
# TYPE wildfly_datasources_pool_xarecover_count gauge
wildfly_datasources_pool_xarecover_count{data_source="KeycloakDS",microprofile_scope="vendor"} 0.0
wildfly_datasources_pool_max_wait_time{data_source="ExampleDS",microprofile_scope="vendor"} 0.0
# HELP wildfly_datasources_pool_destroyed_count The destroyed count
# TYPE wildfly_datasources_pool_destroyed_count gauge
wildfly_datasources_pool_destroyed_count{data_source="KeycloakDS",microprofile_scope="vendor"} 0.0
# HELP wildfly_jca_local_startwork_accepted Number of startWork calls accepted
# TYPE wildfly_jca_local_startwork_accepted gauge
wildfly_jca_local_startwork_accepted{workmanager="default",microprofile_scope="vendor"} 0.0
wildfly_datasources_pool_xaforget_average_time{data_source="KeycloakDS",microprofile_scope="vendor"} 0.0
# HELP wildfly_datasources_pool_xastart_total_time The total time for all XAResource start invocations
# TYPE wildfly_datasources_pool_xastart_total_time gauge
wildfly_datasources_pool_xastart_total_time{data_source="ExampleDS",microprofile_scope="vendor"} 0.0
wildfly_datasources_pool_xarollback_average_time{data_source="KeycloakDS",microprofile_scope="vendor"} 0.0
wildfly_datasources_pool_xaforget_max_time{data_source="ExampleDS",microprofile_scope="vendor"} 0.0
# HELP wildfly_datasources_pool_total_usage_time The total time spent using physical connections
# TYPE wildfly_datasources_pool_total_usage_time gauge
wildfly_datasources_pool_total_usage_time{data_source="KeycloakDS",microprofile_scope="vendor"} 10436.0
wildfly_datasources_pool_xastart_total_time{data_source="KeycloakDS",microprofile_scope="vendor"} 1.0
wildfly_undertow_bytes_received_total_bytes{https_listener="https",server="default-server",microprofile_scope="vendor"} 0.0
# HELP wildfly_datasources_pool_xaprepare_total_time The total time for all XAResource prepare invocations
# TYPE wildfly_datasources_pool_xaprepare_total_time gauge
wildfly_datasources_pool_xaprepare_total_time{data_source="ExampleDS",microprofile_scope="vendor"} 0.0
# HELP wildfly_jca_local_schedulework_rejected Number of scheduleWork calls rejected
# TYPE wildfly_jca_local_schedulework_rejected gauge
wildfly_jca_local_schedulework_rejected{workmanager="default",microprofile_scope="vendor"} 0.0
wildfly_undertow_request_count_total{http_listener="default",server="default-server",microprofile_scope="vendor"} 47.0
# HELP wildfly_datasources_pool_xarollback_total_time The total time for all XAResource rollback invocations
# TYPE wildfly_datasources_pool_xarollback_total_time gauge
wildfly_datasources_pool_xarollback_total_time{data_source="ExampleDS",microprofile_scope="vendor"} 0.0
wildfly_datasources_pool_total_usage_time{data_source="ExampleDS",microprofile_scope="vendor"} 0.0
# HELP wildfly_datasources_pool_max_creation_time The maximum time for creating a physical connection
# TYPE wildfly_datasources_pool_max_creation_time gauge
wildfly_datasources_pool_max_creation_time{data_source="ExampleDS",microprofile_scope="vendor"} 0.0
wildfly_datasources_pool_xaend_average_time{data_source="KeycloakDS",microprofile_scope="vendor"} 0.0
wildfly_datasources_pool_max_creation_time{data_source="KeycloakDS",microprofile_scope="vendor"} 228.0
wildfly_datasources_pool_xarollback_total_time{data_source="KeycloakDS",microprofile_scope="vendor"} 0.0
wildfly_datasources_pool_max_used_count{data_source="ExampleDS",microprofile_scope="vendor"} 0.0
wildfly_datasources_pool_xaprepare_total_time{data_source="KeycloakDS",microprofile_scope="vendor"} 0.0
# HELP wildfly_jca_local_schedulework_accepted Number of scheduleWork calls accepted
# TYPE wildfly_jca_local_schedulework_accepted gauge
wildfly_jca_local_schedulework_accepted{workmanager="default",microprofile_scope="vendor"} 0.0
# HELP wildfly_undertow_max_active_sessions The maximum allowed number of concurrent sessions that this session manager supports
# TYPE wildfly_undertow_max_active_sessions gauge
wildfly_undertow_max_active_sessions{deployment="keycloak-server.war",subdeployment="keycloak-server.war",microprofile_scope="vendor"} -1.0
wildfly_datasources_pool_xacommit_total_time{data_source="ExampleDS",microprofile_scope="vendor"} 0.0
wildfly_undertow_processing_time_total_seconds{https_listener="https",server="default-server",microprofile_scope="vendor"} 0.0
# HELP vendor_memoryPool_CodeHeap_non_profiled_nmethods_usage_max_bytes Peak usage of the CodeHeap 'non-profiled nmethods' memory pool
# TYPE vendor_memoryPool_CodeHeap_non_profiled_nmethods_usage_max_bytes gauge
vendor_memoryPool_CodeHeap_non_profiled_nmethods_usage_max_bytes 1.1455104E7
# HELP wildfly_jca_local_dowork_accepted Number of doWork calls accepted
# TYPE wildfly_jca_local_dowork_accepted gauge
wildfly_jca_local_dowork_accepted{workmanager="default",microprofile_scope="vendor"} 0.0
# HELP wildfly_undertow_sessions_created_total Total sessions created
# TYPE wildfly_undertow_sessions_created_total counter
wildfly_undertow_sessions_created_total{deployment="keycloak-server.war",subdeployment="keycloak-server.war",microprofile_scope="vendor"} 0.0
# HELP wildfly_jca_local_dowork_rejected Number of doWork calls rejected
# TYPE wildfly_jca_local_dowork_rejected gauge
wildfly_jca_local_dowork_rejected{workmanager="default",microprofile_scope="vendor"} 0.0
wildfly_datasources_pool_destroyed_count{data_source="ExampleDS",microprofile_scope="vendor"} 0.0
wildfly_datasources_pool_total_get_time{data_source="KeycloakDS",microprofile_scope="vendor"} 236.0
wildfly_undertow_bytes_sent_total_bytes{http_listener="default",server="default-server",microprofile_scope="vendor"} 617408.0
wildfly_datasources_pool_xarecover_count{data_source="ExampleDS",microprofile_scope="vendor"} 0.0
wildfly_datasources_pool_xaprepare_average_time{data_source="KeycloakDS",microprofile_scope="vendor"} 0.0
# HELP vendor_BufferPool_used_memory_mapped_bytes The memory used by the NIO pool: mapped
# TYPE vendor_BufferPool_used_memory_mapped_bytes gauge
vendor_BufferPool_used_memory_mapped_bytes 0.0
wildfly_datasources_pool_timed_out{data_source="ExampleDS",microprofile_scope="vendor"} 0.0
```