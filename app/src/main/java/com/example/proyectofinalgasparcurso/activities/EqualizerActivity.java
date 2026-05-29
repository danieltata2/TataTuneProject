package com.example.proyectofinalgasparcurso.activities;

import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.media.audiofx.Virtualizer;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.proyectofinalgasparcurso.R;

public class EqualizerActivity extends AppCompatActivity {

    Equalizer equalizer;
    BassBoost bassBoost;
    Virtualizer virtualizer;
    TextView txtInfo;
    SeekBar[] barras;
    SeekBar seek1, seek2, seek3, seek4, seek5, bass, virt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_equalizer);

        // Si no hay musica se cierra
        if (MiniPlayerManager.mediaPlayerGlobal == null) {
            finish();
            return;
        }
        equalizer = MiniPlayerManager.equalizerGlobal;
        bassBoost = MiniPlayerManager.bassGlobal;
        virtualizer = MiniPlayerManager.virtualizerGlobal;
        bass = findViewById(R.id.seekBass);
        virt = findViewById(R.id.seekVirtualizer);
        seek1 = findViewById(R.id.seekBand1);
        seek2 = findViewById(R.id.seekBand2);
        seek3 = findViewById(R.id.seekBand3);
        seek4 = findViewById(R.id.seekBand4);
        seek5 = findViewById(R.id.seekBand5);
        txtInfo = findViewById(R.id.txtBandInfo);

        if (equalizer == null) {
            finish();
            return;
        }
        equalizer.setEnabled(true);
        if (bassBoost != null) {
            bassBoost.setEnabled(true);
        }
        if (virtualizer != null) {
            virtualizer.setEnabled(true);
        }

        //Configuramos las bandas, el bassboost y el virtualizer
        configurarBandas();
        configurarBassBoost();
        configurarVirtualizer();
    }


    //Se usa short porque asi esta definido en la clase del
    // Equalizer no porque lo elijo yo
    //Este metodo conecta las sekBars con el
    // ecualizador para poder controlar el audio y posteriormente guardarlas
    // en un sharedpreferences
    private void configurarBandas() {
        // Obtiene cuantas bandas tiene el ecualizador (ej: 5 bandas)
        short numBandas = equalizer.getNumberOfBands();
        // Obtiene el rango minimo y maximo de cada banda (volumen de ecualizacion)
        short min = equalizer.getBandLevelRange()[0];
        short max = equalizer.getBandLevelRange()[1];
        // Muestra en pantalla cuantas bandas tiene el ecualizador
        txtInfo.setText("Bandas: " + numBandas);
        //Guarda todas las SeekBars en un arreglo para manejarlas facilmente
        barras = new SeekBar[]{seek1, seek2, seek3, seek4, seek5};
        // Recorre cada banda del ecualizador
        for (short i = 0; i < numBandas; i++) {
            // Se crea una copia de i para usarla dentro del listener
            // (evita problemas con el valor del loop)
            short banda = i;
            // Ajusta el maximo de la barra segun el rango del ecualizador
            barras[i].setMax(max - min);
            // Coloca la posicion inicial de la barra segun el valor actual de la banda
            barras[i].setProgress(equalizer.getBandLevel(banda) - min);
            // Escucha cambios en cada SeekBar
            barras[i].setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    // Cambia el nivel de la banda del ecualizador en tiempo real
                    equalizer.setBandLevel(banda, (short) (progress + min));
                }
                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });
        }
    }

    private void configurarBassBoost() {
        // Valor maximo que puede tener la barra de graves del seekbar
        bass.setMax(1000);
        // Obtenemos el valor guardado de los graves desde sharedPreferences
        // y si no existe usa 0 por defecto
        int bassValue = getSharedPreferences("EQ", MODE_PRIVATE)
                .getInt("bass", 0);
        // Lo mismo en el efecto bassBoost (graves)
        bassBoost.setStrength((short) bassValue);
        // Mueve la barra del seekbar al valor guardado
        bass.setProgress(bassValue);
        // Cuando el usuario mueve la barra el seekbar va a escucharlo
        bass.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Cambia la intensidad de los graves
                bassBoost.setStrength((short) progress);
                // Guarda el nuevo valor en sharedPreferences para no perderlo
                // una vez que se sale de la vista para guardar las configuraciones
                // del usuario basicamente
                getSharedPreferences("EQ", MODE_PRIVATE)
                        .edit()
                        .putInt("bass", progress)
                        .apply();
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private void configurarVirtualizer() {
        // Valor maximo que puede tener la barra del virtualizer
        virt.setMax(1000);
        // Obtenemos el valor guardado del virtualizer desde sharedPreferences
        // y si no existe usa 0 por defecto
        int virtValue = getSharedPreferences("EQ", MODE_PRIVATE)
                .getInt("virtualizer", 0);
        // aplicamos el valor que guardamos del efecto virtualizer
        virtualizer.setStrength((short) virtValue);
        // mueve la barra del seekbar al valor guardado
        virt.setProgress(virtValue);
        // escuchara los cambios de la seekBar cuando el usuario la mueve
        virt.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Cambia la intensidad del virtualizer
                virtualizer.setStrength((short) progress);
                // Aqui tambien guardamos el nuevo valor en sharedPreferences para no perderlo
                // una vez que se sale de la vista para guardar las configuraciones
                // del usuario basicamente
                getSharedPreferences("EQ", MODE_PRIVATE)
                        .edit()
                        .putInt("virtualizer", progress)
                        .apply();
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}