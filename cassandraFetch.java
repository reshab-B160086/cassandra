// this function queries cassandra based on start date and end date. Partition key is each day. so for 1 month data 30 async queries are send. 
// function works well until 1 thread is invoking this method but if more threads come then the time delays for all threads.
public List<ConversationDetail> getConversationDetailByCreateDateAndCmId(Timestamp startDateTimestamp, Timestamp endDateTimestamp, int cmId) {

        LocalDate startDate = startDateTimestamp.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endDate = endDateTimestamp.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        List<ConversationDetail> results = new ArrayList<>();
        List<ResultSetFuture> futures = new ArrayList<>();
        try {
         // Async queries begins for each day as partitoion key.
            for (LocalDate date = startDate.plusDays(1); date.isBefore(endDate); date = date.plusDays(1)) {
                ResultSetFuture resultSetFuture = session.executeAsync(findByCreateDayAndCmId.bind(date, cmId).setReadTimeoutMillis(20000));
                futures.add(resultSetFuture);
            }
            futures.add(session.executeAsync(findByCreateDayAndCmIdAndTimestampGreaterThanEqual.bind(startDate, cmId, startDateTimestamp).setReadTimeoutMillis(20000)));
            futures.add(session.executeAsync(findByCreateDayAndCmIdAndTimestampLessThanEqual.bind(endDate, cmId, endDateTimestamp).setReadTimeoutMillis(20000)));
        // Async queries ends.
            for (ResultSetFuture future : futures) {
                try {
                    ResultSet rows = future.getUninterruptibly();
                    Iterator<ConversationDetail> it = mapper.map(rows).iterator();
                    while (it.hasNext()){
                        results.add(it.next());
                    }
                }catch (Exception e){
                    System.out.println("Exception1 " + e.getMessage());
                }
            }
        }catch (Exception e){
            System.out.println("Exception2 "+e.getMessage());
        }
        return results;
    }
