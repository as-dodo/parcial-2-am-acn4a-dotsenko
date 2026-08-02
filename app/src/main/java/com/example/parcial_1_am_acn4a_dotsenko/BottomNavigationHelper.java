package com.example.parcial_1_am_acn4a_dotsenko;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
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
        configureAccessibility(activity, menuInicio, R.string.menu_inicio);
        configureAccessibility(activity, menuCalendario, R.string.menu_calendario);
        configureAccessibility(activity, menuAmigos, R.string.menu_amigos);
        configureAccessibility(activity, menuPerfil, R.string.menu_perfil);

        LinearLayout selectedMenu = activity.findViewById(selectedMenuId);
        if (selectedMenu != null) {
            selectedMenu.setBackgroundResource(R.drawable.bottom_nav_active_bg);
            selectedMenu.setSelected(true);
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
                menuItem.setSelected(false);
            }
        }
    }

    private static void configureAccessibility(
            Activity activity,
            LinearLayout menuItem,
            int labelResource
    ) {
        if (menuItem == null) {
            return;
        }

        menuItem.setContentDescription(activity.getString(labelResource));
        menuItem.setFocusable(true);
        menuItem.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);

        for (int index = 0; index < menuItem.getChildCount(); index++) {
            menuItem.getChildAt(index)
                    .setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        }
    }

    private static void openActivity(Activity currentActivity, Class<?> targetActivity) {
        if (currentActivity.getClass().equals(targetActivity)) {
            return;
        }

        Intent intent = new Intent(currentActivity, targetActivity);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        currentActivity.startActivity(intent);
    }
}
