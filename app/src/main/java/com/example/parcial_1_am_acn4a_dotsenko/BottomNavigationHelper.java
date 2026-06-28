package com.example.parcial_1_am_acn4a_dotsenko;

import android.app.Activity;
import android.content.Intent;
import android.widget.LinearLayout;

public class BottomNavigationHelper {

    private BottomNavigationHelper() {
        // Utility class
    }

    public static void setup(Activity activity, int selectedMenuId) {
        LinearLayout menuInicio = activity.findViewById(R.id.menuInicio);
        LinearLayout menuCalendario = activity.findViewById(R.id.menuCalendario);
        LinearLayout menuAmigos = activity.findViewById(R.id.menuAmigos);
        LinearLayout menuPerfil = activity.findViewById(R.id.menuPerfil);

        clearSelection(menuInicio, menuCalendario, menuAmigos, menuPerfil);

        LinearLayout selectedMenu = activity.findViewById(selectedMenuId);
        if (selectedMenu != null) {
            selectedMenu.setBackgroundResource(R.drawable.bottom_nav_active_bg);
        }

        menuInicio.setOnClickListener(v -> openActivity(activity, MainActivity.class));
        menuCalendario.setOnClickListener(v -> openActivity(activity, CalendarActivity.class));
        menuAmigos.setOnClickListener(v -> openActivity(activity, FriendsActivity.class));
        menuPerfil.setOnClickListener(v -> openActivity(activity, ProfileActivity.class));
    }

    private static void clearSelection(LinearLayout... menuItems) {
        for (LinearLayout menuItem : menuItems) {
            if (menuItem != null) {
                menuItem.setBackgroundResource(0);
            }
        }
    }

    private static void openActivity(Activity currentActivity, Class<?> targetActivity) {
        if (currentActivity.getClass().equals(targetActivity)) {
            return;
        }

        Intent intent = new Intent(currentActivity, targetActivity);
        currentActivity.startActivity(intent);
    }
}
