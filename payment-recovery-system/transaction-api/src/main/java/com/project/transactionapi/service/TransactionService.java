@Service
public class TransactionService {
  private final TransactionRepository repo;
  public TransactionService(TransactionRepository repo){ this.repo=repo; }

  public Transaction createTransaction(double amount){
    Transaction t=new Transaction();
    t.setTxnId(UUID.randomUUID().toString());
    t.setAmount(amount);
    t.setStatus("PENDING");
    return repo.save(t);
  }

  public Transaction processTransaction(String txnId){
    Transaction t=repo.findByTxnId(txnId).orElseThrow();
    boolean success=Math.random()>0.3;
    t.setStatus(success?"SUCCESS":"FAILED");
    if(!success) t.setRetryCount(t.getRetryCount()+1);
    return repo.save(t);
  }
}
