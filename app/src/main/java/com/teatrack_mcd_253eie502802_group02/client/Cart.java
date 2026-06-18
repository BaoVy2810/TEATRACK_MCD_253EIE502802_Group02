<<<<<<<< HEAD:app/src/main/java/com/teatrack_mcd_253eie502802_group02/MainActivity.java
package com.teatrack_mcd_253eie502802_group02;
========
package com.teatrack_mcd_253eie502802_group02.client;
>>>>>>>> main:app/src/main/java/com/teatrack_mcd_253eie502802_group02/client/Cart.java

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.teatrack_mcd_253eie502802_group02.R;

<<<<<<<< HEAD:app/src/main/java/com/teatrack_mcd_253eie502802_group02/MainActivity.java
public class MainActivity extends AppCompatActivity {
========
public class Cart extends AppCompatActivity {
>>>>>>>> main:app/src/main/java/com/teatrack_mcd_253eie502802_group02/client/Cart.java

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cart);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}