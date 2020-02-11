package com.example.founders;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.Toast;

import com.samsung.android.sdk.blockchain.CoinType;
import com.samsung.android.sdk.blockchain.ListenableFutureTask;
import com.samsung.android.sdk.blockchain.SBlockchain;
import com.samsung.android.sdk.blockchain.account.Account;
import com.samsung.android.sdk.blockchain.account.ethereum.EthereumAccount;
import com.samsung.android.sdk.blockchain.coinservice.CoinNetworkInfo;
import com.samsung.android.sdk.blockchain.coinservice.CoinServiceFactory;
import com.samsung.android.sdk.blockchain.coinservice.ethereum.EthereumService;
import com.samsung.android.sdk.blockchain.exception.SsdkUnsupportedException;
import com.samsung.android.sdk.blockchain.network.EthereumNetworkType;
import com.samsung.android.sdk.blockchain.ui.CucumberWebView;
import com.samsung.android.sdk.blockchain.ui.OnSendTransactionListener;
import com.samsung.android.sdk.blockchain.wallet.HardwareWallet;
import com.samsung.android.sdk.blockchain.wallet.HardwareWalletType;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity implements OnSendTransactionListener {

    Button connectBtn;
    Button generateAccountBtn;
    Button getAccountsBtn;
    Button paymentSheetBtn;
    Button sendSmartContractBtn;
    Button webViewInitBtn;

    SBlockchain sBlockchain = new SBlockchain();
    private HardwareWallet wallet;
    private Account generatedAccount;
    private CucumberWebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            sBlockchain.initialize(this);
        } catch (SsdkUnsupportedException e) {
            e.printStackTrace();
        }

        connectBtn = findViewById(R.id.connect);
        generateAccountBtn = findViewById(R.id.generateAccount);
        getAccountsBtn = findViewById(R.id.getAccounts);
        paymentSheetBtn = findViewById(R.id.paymentsheet);
        sendSmartContractBtn = findViewById(R.id.sendSmartContract);
        webViewInitBtn = findViewById(R.id.webViewInit);
        webView = findViewById(R.id.cucumberWebView);

        connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connect();
            }
        });

        generateAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generate();
            }
        });

        getAccountsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getAccounts();
            }
        });

        paymentSheetBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                paymentSheet();
            }
        });

        sendSmartContractBtn.setOnClickListener((new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                sendSmartContract();
            }
        }));

        webViewInitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webViewInit();
            }
        });
    }

    private void webViewInit() {
        List<Account> accounts = sBlockchain.getAccountManager()
                .getAccounts(wallet.getWalletId(),
                        CoinType.ETH,
                        EthereumNetworkType.ROPSTEN);

        CoinNetworkInfo coinNetworkInfo = new CoinNetworkInfo(
                CoinType.ETH,
                EthereumNetworkType.ROPSTEN,
                "https://ropsten.infura.io/v3/70ddb1f89ca9421885b6268e847a459d");

        EthereumService ethService = (EthereumService) CoinServiceFactory.getCoinService(this, coinNetworkInfo);

        webView.init(ethService, accounts.get(0), this::onSendTransaction);
        webView.loadUrl("https://faucet.metamask.io");
    }

    // TODO : sendSmartContract 작성
    private void sendSmartContract(){

    }

    private void paymentSheet(){
        CoinNetworkInfo coinNetworkInfo = new CoinNetworkInfo(
                CoinType.ETH,
                EthereumNetworkType.ROPSTEN,
                "https://ropsten.infura.io/v3/70ddb1f89ca9421885b6268e847a459d");

        List<Account> accounts = sBlockchain.getAccountManager()
                .getAccounts(wallet.getWalletId(),
                        CoinType.ETH,
                        EthereumNetworkType.ROPSTEN);

        EthereumService ethereumService = (EthereumService) CoinServiceFactory.getCoinService(this, coinNetworkInfo);
        Intent intent =  ethereumService.createEthereumPaymentSheetActivityIntent(
                this,
                wallet,
                (EthereumAccount) accounts.get(0),
                "0x7526A6232Be6c7234bF53A3ce87Bda7d2FC530c1",
                new BigInteger("10000000000000000"),
                null,
                null);

        startActivityForResult(intent, 0);
    }

    private void getAccounts(){
        List<Account> accounts = sBlockchain.getAccountManager()
                .getAccounts(wallet.getWalletId(), CoinType.ETH, EthereumNetworkType.ROPSTEN);
        Log.d("MyApp", Arrays.toString(new List[]{accounts}));
    }

    private void generate() {
        // RPC address : infura.io
        CoinNetworkInfo coinNetworkInfo = new CoinNetworkInfo(
                CoinType.ETH,
                EthereumNetworkType.ROPSTEN,
                "https://ropsten.infura.io/v3/70ddb1f89ca9421885b6268e847a459d");

        // accountManager가 필요하고 관리자들은 모두 sBlockchain 에서 얻을 수 있다.
        // 서버를 불렀기 때문에 callBack 이 필요함
        sBlockchain.getAccountManager()
                .generateNewAccount(wallet, coinNetworkInfo)
                .setCallback(new ListenableFutureTask.Callback<Account>() {
                    @Override
                    public void onSuccess(Account account) {
                        generatedAccount = account;
//                        Toast.makeText(this, account.toString(), Toast.LENGTH_SHORT).show();
                        Log.d("MyApp", account.toString());
                    }

                    @Override
                    public void onFailure(@NotNull ExecutionException e) {

                    }

                    @Override
                    public void onCancelled(@NotNull InterruptedException e) {

                    }
                });
    }

    // 콜드월렛 불러오기
    // 비동기로 해야함 usb를 넣었을 때 멈추지않고 실행되려면 별도의 스레드로 실행.
    private void connect(){
        sBlockchain.getHardwareWalletManager()
                .connect(HardwareWalletType.SAMSUNG, true)
                .setCallback(new ListenableFutureTask.Callback<HardwareWallet>() {
                    @Override
                    public void onSuccess(HardwareWallet hardwareWallet) {
                        wallet = hardwareWallet;
                    }

                    @Override
                    public void onFailure(ExecutionException e) {

                    }

                    @Override
                    public void onCancelled(InterruptedException e) {

                    }
                });
    }

    @Override
    public void onSendTransaction(
            @NotNull String requestId,
            @NotNull EthereumAccount fromAccount,
            @NotNull String toAddress,
            @org.jetbrains.annotations.Nullable BigInteger value,
            @org.jetbrains.annotations.Nullable String data,
            @org.jetbrains.annotations.Nullable BigInteger nonce
    ) {
        HardwareWallet connectedHardwareWallet =
                sBlockchain.getHardwareWalletManager().getConnectedHardwareWallet();
        Intent intent =
                webView.createEthereumPaymentSheetActivityIntent(
                        this,
                        requestId,
                        connectedHardwareWallet,
                        toAddress,
                        value,
                        data,
                        nonce
                );

        startActivityForResult(intent, 0);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode != 0) {
            return;
        }

        webView.onActivityResult(requestCode, resultCode, data);
    }
}
