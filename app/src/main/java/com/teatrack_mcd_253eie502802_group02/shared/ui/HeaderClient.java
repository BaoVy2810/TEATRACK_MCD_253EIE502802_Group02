package com.teatrack_mcd_253eie502802_group02.shared.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.PopupMenu;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.client.Cart;
import com.teatrack_mcd_253eie502802_group02.client.Homepage;
import com.teatrack_mcd_253eie502802_group02.client.UserProfile;
import com.teatrack_mcd_253eie502802_group02.util.GoogleSignInHelper;

public class HeaderClient extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_header_client);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        findViewById(R.id.btn_cart).setOnClickListener(v ->
                startActivity(new Intent(this, Cart.class)));

        findViewById(R.id.btn_profile).setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(this, v);
            popup.getMenuInflater().inflate(R.menu.menu_profile_header, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.menu_signout) {
                    GoogleSignInHelper.signOutGoogle(this);
                    com.google.firebase.auth.FirebaseAuth.getInstance().signOut();
                    com.teatrack_mcd_253eie502802_group02.util.UserProfileHelper.clearSession(this);
                    Intent intent = new Intent(this, com.teatrack_mcd_253eie502802_group02.MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                    return true;
                }
                return false;
            });
            popup.show();
        });
    }
    public void openClientHomepage(View view) {
        startActivity(new Intent(this, Homepage.class));
    }
}
