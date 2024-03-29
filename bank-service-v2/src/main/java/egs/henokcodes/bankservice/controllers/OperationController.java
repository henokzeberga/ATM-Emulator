package egs.henokcodes.bankservice.controllers;

import egs.henokcodes.bankservice.dto.*;
import egs.henokcodes.bankservice.models.Account;
import egs.henokcodes.bankservice.models.Transaction;
import egs.henokcodes.bankservice.repository.AccountRepository;
import egs.henokcodes.bankservice.services.MyUserDetailsService;

import egs.henokcodes.bankservice.services.OperationService;
import egs.henokcodes.bankservice.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping( path = "/api/v1/transactions")
public class OperationController {
    Logger logger = LoggerFactory.getLogger(OperationController.class);

    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtTokenUtil;
    @Autowired
    private OperationService operationService;

    @Autowired
    private MyUserDetailsService userDetailsService;


    @PostMapping(value = "/withdraw")
    public ResponseEntity<TransactionResponse> withdraw(@RequestHeader("Authorization") String token,@RequestBody TransactionRequest transactionRequest) throws Exception {
       return this.operationService.withdraw(token,transactionRequest);
    }
    @PostMapping(value = "/deposit")
    public ResponseEntity<TransactionResponse> deposit(@RequestHeader("Authorization") String token,@RequestBody TransactionRequest transactionRequest) throws Exception {
        return this.operationService.deposit(token,transactionRequest);
    }
    @PostMapping(value = "/balance")
    public ResponseEntity<QueryResponse> balance(@RequestHeader("Authorization") String token,@RequestBody QueryRequest queryRequest) throws Exception {
        return this.operationService.balance(token,queryRequest);
    }
    @PostMapping(value = "/statement")
    public ResponseEntity<StatementResponse> statement(@RequestHeader("Authorization") String token,@RequestBody QueryRequest queryRequest) throws Exception {
        return this.operationService.statement(token,queryRequest);
    }


    @PostMapping(value = "/authenticate")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthenticationRequest authenticationRequest) throws Exception {
        Account acc = this.accountRepository.findAccountByCardNumber(authenticationRequest.getCardNumber());
        Account account = new Account();
        if (authenticationRequest.getRemainingAttempt() > 0) {
            try {
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(authenticationRequest.getCardNumber(), authenticationRequest.getPassword())
                );
                account.setRemainingAttempt(3);
            } catch (BadCredentialsException e) {
               account.setRemainingAttempt(acc.getRemainingAttempt()-1);
                throw new Exception("Incorrect credentials", e);
            }
            final UserDetails userDetails = userDetailsService
                    .loadUserByUsername(authenticationRequest.getCardNumber());

            final String jwt = jwtTokenUtil.generateToken(userDetails);

            return ResponseEntity.ok(new AuthenticationResponse(jwt));
        }else{
            return ResponseEntity.badRequest().body("Card Blocked");
        }
    }

}
