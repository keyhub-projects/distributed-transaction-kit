
```mermaid
---
title: KhTransaction
---
classDiagram
    class KhTransaction {
        KhTransactionId getTransactionId()
        setCompensation(KhTransaction compensation)
        setOutbox(KhTransaction outbox)
        Result resolve()
    }
    <<interface>> KhTransaction

    class Result {
        toData()
        toList()
    }
    <<interface>> Result
    KhTransaction <-- Result
    
    class AbstractTransaction {
        KhTransactionId transactionId
        CompensatingTransactionStore compensatingTransactionStore
        OutboxTransactionStore outboxTransactionStore
    }
    <<abstract>> AbstractTransaction
    KhTransaction <|.. AbstractTransaction
    
    class SingleTransaction {
        
    }
    <<interface>> SingleTransaction
    KhTransaction <|-- SingleTransaction
    
    class AbstractSingleTransaction {
        KhTransaction compensationTransaction
        KhTransaction outboxTransaction
        RemoteTransactionException exception
    }
    <<abstract>> AbstractSingleTransaction
    SingleTransaction <|.. AbstractSingleTransaction
    AbstractTransaction <|-- AbstractSingleTransaction
    
    class SimpleSingleTransaction {
        Supplier<R> transactionProcess
    }
    AbstractSingleTransaction <|-- SimpleSingleTransaction
    
    
    class RemoteTransaction {
        of()
        get()
        post()
        put()
        delete()
        request()
    }
    <<interface>> RemoteTransaction
    SingleTransaction <|-- RemoteTransaction
    
    class AbstractRemoteTransaction {
        ObjectMapper objectMapper
    }
    <<abstract>> AbstractRemoteTransaction
    RemoteTransaction <|.. AbstractRemoteTransaction
    AbstractSingleTransaction <|-- AbstractRemoteTransaction

    class SimpleRemoteTransaction {
    }
    AbstractRemoteTransaction <|-- SimpleRemoteTransaction




    class CompositeTransaction {
    }
    KhTransaction <|-- CompositeTransaction
    <<interface>> CompositeTransaction

    class SimpleCompositeTransaction {
        Map<KhTransactionId, KhTransaction> transactionMap
    }
    CompositeTransaction <|.. SimpleCompositeTransaction
    AbstractTransaction <|-- SimpleCompositeTransaction

    class SequencedTransaction {
    }
    <<interface>> SequencedTransaction
    CompositeTransaction <|-- SequencedTransaction

    class SimpleSequencedTransaction {
        List<KhTransactionId> transactionSequence
    }
    SequencedTransaction <|.. SimpleSequencedTransaction
    SimpleCompositeTransaction <|-- SimpleSequencedTransaction
```

```mermaid
---
title: compensation flow
---
flowchart
    start([start transaction])
    transaction(transact KhTransaction)
    storeTransactionId(store TransactionId, CompensatingTransactions pair in stack)
    exception(exception invoked)
    handleByInterceptor(handle by transaction interceptor)
    wal(write ahead log)
    compensateByStore(compensate by store)
    start --> transaction --> isInTransaction --> storeTransactionId --> exception --> handleByInterceptor --> wal --> compensateByStore
```

```mermaid
---
title: transaction outbox flow
---
flowchart
    start([start transaction])
    transaction(transact KhTransaction)
    storeTransactionId(store TransactionId, outboxTransaction pair in stack)
    finishTransaction(transaction finished)
    handleByInterceptor(handle by transaction interceptor)
    invokeOutboxEventByStore(invoke outbox event by store)
    start --> transaction --> isInTransaction --> storeTransactionId --> finishTransaction --> handleByInterceptor --> invokeOutboxEventByStore
```

- Transaction에 의해 관리된다면, 인터셉터가 Transaction을 바라보도록 트랜잭션 범위를 확장한다.
- 없다면 단일 트랜잭션으로 처리
