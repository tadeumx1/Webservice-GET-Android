package com.matheustadeu.aula_12;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.rtoshiro.util.format.SimpleMaskFormatter;
import com.github.rtoshiro.util.format.text.MaskTextWatcher;

import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private EditText edtCEP;
    private TextView txvCEP;

    // Obejtos complementares
    private View global = null;
    private String CEP;
    private String captura = "";
    private ProgressDialog progressDialog;
    private InputStream inputStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edtCEP = (EditText) findViewById(R.id.edtCEP);
        txvCEP = (TextView) findViewById(R.id.txvCEP);

        SimpleMaskFormatter smf = new SimpleMaskFormatter("NNNNN-NNN");
        MaskTextWatcher mtw = new MaskTextWatcher(edtCEP, smf);
        edtCEP.addTextChangedListener(mtw);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Fazer a validação do EditText

                pesquisarCEP(view);

                Snackbar.make(view, "Digite o CEP ! ", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

    }

    public void pesquisarCEP(View view) {

        // -- Neutralizar a View do app

        global = view;
        // Com isso tira a ação de todos os componentes do aplicativo
        global.setEnabled(false);

        // - Capturando o CEP

        CEP = edtCEP.getEditableText().toString().trim();

        // -- Adicionando a sicronização

        AsyncTask<Void, Void, Void> severino = new AsyncTask<Void, Void, Void>() {

            // -- Método antes da chamada

            @Override
            protected void onPreExecute() {

                // -- Adicionar a ampulheta ProgressDialog

                progressDialog = new ProgressDialog(global.getContext());
                progressDialog.setTitle("Pesquisa de CEP");
                progressDialog.setMessage("Aguarde ! Processando os dados ! ");
                progressDialog.setCancelable(false);
                progressDialog.setIndeterminate(true);
                progressDialog.show();

            }

            // -- Método que roda enquanto está processando

            @Override
            protected Void doInBackground(Void... voids) {

                try {

                    // - Aguardar 2 seg
                    Thread.sleep(2000);

                    // - Montar a URL para pesquisa

                    String webservice = "http://api.postmon.com.br/v1/cep/" + CEP;

                    URL url = new URL(webservice);

                    // - Conexão com o protcolo HTTTP

                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                    // -Regras de conexão

                    // Depois de conectar ele vai ter esse tempo para ler os dados

                    connection.setReadTimeout(10000);

                    // Ele vai conectar ao WebService

                    connection.setConnectTimeout(15000);

                    connection.setRequestMethod("GET");
                    connection.setDoInput(true);

                    // - Conectando

                    connection.connect();

                    // - Capturar o retorno

                    int codigo = connection.getResponseCode();
                    Log.i("CEP", "" + codigo);

                    // - Recuperar todo o conteúdo

                    inputStream = connection.getInputStream();

                    // - Converter o Stream em texto ele está convertendo o JSON para UTF-8

                    Reader reader = new InputStreamReader(inputStream, "UTF-8");
                    BufferedReader bufferedReader = new BufferedReader(reader);
                    StringBuffer stringBuffer = new StringBuffer();

                    /* O captura está sendo usado para ver se tem algo vazio no dado q
                     que foi recebido e isso quer dizer quando voltar vazio é que tem algum erro */

                    while ((captura = bufferedReader.readLine()) != null) {

                        stringBuffer.append(captura);

                    }

                    captura = stringBuffer.toString();

                    // - Fechar a conexão

                    inputStream.close();

                } catch (Exception erro) {

                    Log.e("ERRO", erro.getMessage());

                }

                return null;
            }

            // -- Método executado após a chamada

            @Override
            protected void onPostExecute(Void aVoid) {

                // - Finalizar a tela de processamento

                if (progressDialog != null) {

                    progressDialog.dismiss();
                    global.setEnabled(true);

                    // - Extair o conteúdo

                        /* A JSON Object vai ser a classe que realmente vai ler e deixar
                         a gente manipular o arquivo JSON com os dados */

                    StringBuffer stringBuffer = new StringBuffer();

                    stringBuffer.append(verificaCep("Endereço: ", "logradouro"));
                    stringBuffer.append("\n" + verificaCep("Bairro: ", "bairro"));
                    stringBuffer.append("\n" + verificaCep("Cidade: ", "cidade"));
                    stringBuffer.append("\n" + verificaCep("Estado: ", "estado"));
                    stringBuffer.append("\n" + verificaCep("Cep: ", "cep"));

                    txvCEP.setText(stringBuffer.toString());

                        /* Caso eu quiser exibir os dados em cada EditText ou em outros componente será
                         necessário fazer um StringBuffer para receber os dados para cada componente */

                }

            }
        };

        severino.execute((Void[]) null);
        edtCEP.setText("");

    }

    private String verificaCep(String cep0, String cep1) {

        String cepp = "";

        try {

            // Extrair o conteúdo

            JSONObject jsonObject = new JSONObject(captura);

            cepp = jsonObject.getString(cep1);


        } catch (Exception erro) {

            Log.e("ERRO", erro.getMessage());
        }

        if (cepp.isEmpty()) {
            cepp = "";
        } else {
            cepp = cep0 + cepp;
        }

        return cepp;

    }

};
