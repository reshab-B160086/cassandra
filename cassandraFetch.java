 public List<ConversationDetail> getConversationDetailByCreateDateAndCmId(Timestamp startDateTimestamp, Timestamp endDateTimestamp, int cmId) {

        LocalDate startDate = startDateTimestamp.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endDate = endDateTimestamp.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        List<ConversationDetail> results = new ArrayList<>();
        List<ResultSetFuture> futures = new ArrayList<>();
        try {
            for (LocalDate date = startDate.plusDays(1); date.isBefore(endDate); date = date.plusDays(1)) {
                ResultSetFuture resultSetFuture = session.executeAsync(findByCreateDayAndCmId.bind(date, cmId).setReadTimeoutMillis(20000));
                futures.add(resultSetFuture);
            }
            futures.add(session.executeAsync(findByCreateDayAndCmIdAndTimestampGreaterThanEqual.bind(startDate, cmId, startDateTimestamp).setReadTimeoutMillis(20000)));
            futures.add(session.executeAsync(findByCreateDayAndCmIdAndTimestampLessThanEqual.bind(endDate, cmId, endDateTimestamp).setReadTimeoutMillis(20000)));

            Instant time1 = Instant.now();
            for (ResultSetFuture future : futures) {
                try {
                    ResultSet rows = future.getUninterruptibly();
                    Iterator<ConversationDetail> it = mapper.map(rows).iterator();
                    while (it.hasNext()){
                        results.add(it.next());
                    }
                    Instant end = Instant.now();
                }catch (Exception e){
                    System.out.println("Exception1 " + e.getMessage());
                }
            }
        }catch (Exception e){
            System.out.println("Exception2 "+e.getMessage());
        }
        return results;
    }
