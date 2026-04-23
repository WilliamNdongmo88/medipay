package com.medipay.unit;

import com.medipay.entity.*;
import com.medipay.enums.TransactionStatus;
import com.medipay.enums.TransactionType;
import com.medipay.repository.TransactionRepository;
import com.medipay.repository.WalletRepository;
import com.medipay.service.NotificationService;
import com.medipay.service.PaymentService;
import com.medipay.service.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private NotificationService notificationService;

    @Spy
    @InjectMocks
    private PaymentService paymentService;

    private User client;
    private User pharmacist;
    private User admin;
    private Wallet clientWallet;
    private Wallet pharmacistWallet;
    private Wallet adminWallet;

    private static final String QR_CODE = "QR123";
    private static final BigDecimal INITIAL_BALANCE = BigDecimal.valueOf(10000);
    private static final BigDecimal PAYMENT_AMOUNT = BigDecimal.valueOf(5000);

    @BeforeEach
    void setup() {
        // Initialisation des utilisateurs
        client = createUser(1L, "client");
        pharmacist = createUser(2L, "pharma");
        admin = createUser(3L, "admin");

        // Initialisation des portefeuilles
        clientWallet = createWallet(10L, client, INITIAL_BALANCE);
        pharmacistWallet = createWallet(20L, pharmacist, BigDecimal.ZERO);
        adminWallet = createWallet(30L, admin, INITIAL_BALANCE);
    }

    private User createUser(Long id, String username) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        return user;
    }

    private Wallet createWallet(Long id, User user, BigDecimal balance) {
        Wallet wallet = new Wallet();
        wallet.setId(id);
        wallet.setUser(user);
        wallet.setBalance(balance);
        return wallet;
    }

    private void setupSecurityContext(User user) {
        UserDetailsImpl userDetails = new UserDetailsImpl(
                user.getId(),
                user.getUsername(),
                user.getUsername() + "@mail.com",
                "password",
                Collections.emptyList()
        );

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }

    @Nested
    @DisplayName("Tests pour creditClient")
    class CreditClientTests {

        @Test
        @DisplayName("Devrait créditer le client avec succès par un administrateur")
        void shouldCreditClientSuccessfully() {
            // GIVEN
            setupSecurityContext(admin);
            when(walletRepository.findByUserId(admin.getId())).thenReturn(Optional.of(adminWallet));
            when(walletRepository.findByUserId(client.getId())).thenReturn(Optional.of(clientWallet));
            when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));

            BigDecimal expectedAdminBalance = adminWallet.getBalance().add(PAYMENT_AMOUNT);
            BigDecimal expectedClientBalance = clientWallet.getBalance().add(PAYMENT_AMOUNT);

            // WHEN
            Transaction tx = paymentService.creditClient(client.getId(), PAYMENT_AMOUNT);

            // THEN
            assertNotNull(tx);
            assertEquals(PAYMENT_AMOUNT, tx.getAmount());
            assertEquals(TransactionType.DEPOSIT, tx.getType());
            assertEquals(expectedAdminBalance, adminWallet.getBalance());
            assertEquals(expectedClientBalance, clientWallet.getBalance());

            verify(walletRepository, times(2)).save(any(Wallet.class));
            verify(notificationService).notifyUser(anyString(), eq(admin.getUsername()), eq(clientWallet.getId()), eq("DEPOSIT"));
        }
    }

    @Nested
    @DisplayName("Tests pour processPayment")
    class ProcessPaymentTests {

        @Test
        @DisplayName("Devrait traiter le paiement avec succès")
        void shouldProcessPaymentSuccessfully() {
            // GIVEN
            when(walletRepository.findByUserId(client.getId())).thenReturn(Optional.of(clientWallet));
            when(walletRepository.findByUserId(pharmacist.getId())).thenReturn(Optional.of(pharmacistWallet));
            when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));

            doNothing().when(paymentService).markAsUsed(QR_CODE);
            doNothing().when(paymentService).processTransaction(any());

            BigDecimal expectedClientBalance = clientWallet.getBalance().subtract(PAYMENT_AMOUNT);
            BigDecimal expectedPharmaBalance = pharmacistWallet.getBalance().add(PAYMENT_AMOUNT);

            // WHEN
            Transaction result = paymentService.processPayment(client.getId(), pharmacist.getId(), PAYMENT_AMOUNT, QR_CODE);

            // THEN
            assertNotNull(result);
            assertEquals(PAYMENT_AMOUNT, result.getAmount());
            assertEquals(TransactionStatus.COMPLETED, result.getStatus());
            assertEquals(expectedClientBalance, clientWallet.getBalance());
            assertEquals(expectedPharmaBalance, pharmacistWallet.getBalance());

            verify(walletRepository, times(2)).save(any(Wallet.class));
            verify(paymentService).markAsUsed(QR_CODE);
        }

        @Test
        @DisplayName("Devrait lever une exception si le solde est insuffisant")
        void shouldThrowExceptionIfInsufficientBalance() {
            // GIVEN
            clientWallet.setBalance(BigDecimal.valueOf(1000));
            when(walletRepository.findByUserId(client.getId())).thenReturn(Optional.of(clientWallet));
            when(walletRepository.findByUserId(pharmacist.getId())).thenReturn(Optional.of(pharmacistWallet));

            // WHEN & THEN
            RuntimeException ex = assertThrows(RuntimeException.class, () ->
                    paymentService.processPayment(client.getId(), pharmacist.getId(), PAYMENT_AMOUNT, QR_CODE));

            assertEquals("Solde insuffisant pour effectuer ce paiement", ex.getMessage());
            verify(walletRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Tests pour executeOpenPayment")
    class ExecuteOpenPaymentTests {

        @Test
        @DisplayName("Devrait exécuter un paiement ouvert avec succès")
        void shouldExecuteOpenPaymentSuccessfully() {
            // GIVEN
            when(walletRepository.findByUserId(client.getId())).thenReturn(Optional.of(clientWallet));
            when(walletRepository.findByUserId(pharmacist.getId())).thenReturn(Optional.of(pharmacistWallet));
            when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));
            doNothing().when(paymentService).processTransaction(any());

            // WHEN
            Transaction result = paymentService.executeOpenPayment(client.getId(), pharmacist.getId(), PAYMENT_AMOUNT);

            // THEN
            assertNotNull(result);
            assertEquals(PAYMENT_AMOUNT, result.getAmount());
            assertEquals(TransactionStatus.COMPLETED, result.getStatus());
            assertEquals(TransactionType.PAYMENT, result.getType());

            verify(transactionRepository).save(any(Transaction.class));
        }

        @Test
        @DisplayName("Devrait lever une exception si le montant est invalide")
        void shouldThrowIfAmountZeroOrNegative() {
            // WHEN & THEN
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                    paymentService.executeOpenPayment(client.getId(), pharmacist.getId(), BigDecimal.ZERO));

            assertEquals("Le montant doit être supérieur à zéro.", ex.getMessage());
            verifyNoInteractions(walletRepository);
        }

        @Test
        @DisplayName("Devrait lever une exception si le portefeuille client est manquant")
        void shouldThrowIfClientWalletMissing() {
            // GIVEN
            when(walletRepository.findByUserId(client.getId())).thenReturn(Optional.empty());

            // WHEN & THEN
            assertThrows(RuntimeException.class, () ->
                    paymentService.executeOpenPayment(client.getId(), pharmacist.getId(), PAYMENT_AMOUNT));
        }
    }
}
