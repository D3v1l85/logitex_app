package com.example.logitex_app.utils;

import android.content.Context;

public class TranslationHelper {

    public static String get(Context context, String keyCA, String keyEN) {
        if (context == null) return keyCA;
        String lang = LocaleHelper.getLanguage(context);
        return lang.equalsIgnoreCase("en") ? keyEN : keyCA;
    }

    public static String errorQrMismatch(Context context) {
        return get(context, 
            "Error: El codi QR escanejat no correspon a aquesta ordre!", 
            "Error: The scanned QR code does not correspond to this order!");
    }

    public static String scanCancelled(Context context) {
        return get(context, "Escaneig cancel·lat", "Scan cancelled");
    }

    public static String connectionError(Context context) {
        return get(context, "Error de connexió", "Connection error");
    }

    public static String serverError(Context context) {
        return get(context, "Error al servidor", "Server error");
    }

    public static String routeStarted(Context context) {
        return get(context, "Ruta en marxa!", "Route is now in transit!");
    }

    public static String promptScanTransit(Context context) {
        return get(context, 
            "Enquadra el codi QR de l'ordre per INICIAR RUTA", 
            "Scan the order QR code to START ROUTE");
    }

    public static String promptScanDeliver(Context context) {
        return get(context, 
            "Enquadra el codi QR de l'ordre per LLIURAR", 
            "Scan the order QR code to DELIVER");
    }

    public static String promptScanDefault(Context context) {
        return get(context, "Enquadra el codi QR de l'ordre", "Scan the order QR code");
    }

    public static String orderNotFound(Context context) {
        return get(context, 
            "No s'ha trobat cap ordre amb aquest codi", 
            "No order was found with this code");
    }

    public static String albaraLabel(Context context) {
        return get(context, "Albarà: ", "Delivery note: ");
    }

    public static String direccioLabel(Context context) {
        return get(context, "Direcció: ", "Address: ");
    }

    public static String incidentSent(Context context) {
        return get(context, "Incidència enviada correctament", "Incident reported successfully");
    }

    public static String fillAllFields(Context context) {
        return get(context, "Completa tots els camps", "Please complete all fields");
    }

    public static String incidentSendError(Context context) {
        return get(context, "Error en enviar la incidència", "Error reporting the incident");
    }

    public static String scanScanning(Context context) {
        return get(context, "Escanejant...", "Scanning...");
    }

    public static String enterCodeFirst(Context context) {
        return get(context, "Introdueix un codi primer", "Please enter a code first");
    }

    public static String invalidCode(Context context) {
        return get(context, "Codi buit o no vàlid", "Empty or invalid code");
    }

    public static String searchingOrder(Context context, String code) {
        return get(context, "Buscant ordre " + code + "...", "Searching order " + code + "...");
    }

    public static String orderFound(Context context) {
        return get(context, "Ordre trobada!", "Order found!");
    }

    public static String orderNotFoundOrError(Context context) {
        return get(context, "Ordre no trobada o error", "Order not found or error");
    }

    public static String historyEmpty(Context context) {
        return get(context, "L'historial està buit", "History is empty");
    }

    public static String destiLabel(Context context) {
        return get(context, "Destí: ", "Destination: ");
    }

    public static String delivered(Context context) {
        return get(context, "ENTREGAT", "DELIVERED");
    }

    public static String inTransit(Context context) {
        return get(context, "EN TRÀNSIT", "IN TRANSIT");
    }

    public static String loginOk(Context context, String name) {
        return get(context, "Login OK: " + name, "Logged in as: " + name);
    }

    public static String loginIncorrect(Context context) {
        return get(context, "Email o contrasenya incorrectes", "Incorrect email or password");
    }

    public static String welcomeTitle(Context context) {
        return get(context, "Benvingut", "Welcome");
    }

    public static String userHint(Context context) {
        return get(context, "Usuari", "Username");
    }

    public static String passwordHint(Context context) {
        return get(context, "Contrasenya", "Password");
    }

    public static String loginButtonText(Context context) {
        return get(context, "INICIAR SESSIÓ", "LOG IN");
    }

    public static String historialTitle(Context context) {
        return get(context, "Historial de canvis", "Change History");
    }

    public static String seeOnGoogleMaps(Context context) {
        return get(context, "VEURE A GOOGLE MAPS", "VIEW ON GOOGLE MAPS");
    }

    public static String deliveryAddressTitle(Context context) {
        return get(context, "Adreça de lliurament", "Delivery Address");
    }

    public static String noChangesRegistered(Context context) {
        return get(context, "No hi ha cap canvi registrat.", "No changes registered.");
    }

    public static String modifiedBy(Context context) {
        return get(context, "Modificat per: ", "Modified by: ");
    }

    public static String systemUser(Context context) {
        return get(context, "Sistema", "System");
    }

    public static String dateLabel(Context context) {
        return get(context, "Data: ", "Date: ");
    }

    public static String reportIncidentButton(Context context) {
        return get(context, "REPORTAR INCIDÈNCIA", "REPORT INCIDENT");
    }

    public static String scanTitle(Context context) {
        return get(context, "Escanejar Codi QR", "Scan QR Code");
    }

    public static String scanDescription(Context context) {
        return get(context, 
            "Enquadra el codi QR d'una ordre (de l'albarà o de la pantalla web) per obrir els seus detalls i poder gestionar-la.", 
            "Scan the QR code of an order (from the delivery note or the web dashboard) to view its details and manage it.");
    }

    public static String readyToScan(Context context) {
        return get(context, "Llest per escanejar", "Ready to scan");
    }

    public static String openCamera(Context context) {
        return get(context, "OBRIR CÀMERA", "OPEN CAMERA");
    }

    public static String orWriteManual(Context context) {
        return get(context, "- O escriu el codi manualment -", "- Or write the code manually -");
    }

    public static String manualHint(Context context) {
        return get(context, "ID o Referència", "ID or Reference");
    }

    public static String incidentsTitle(Context context) {
        return get(context, "Reportar Incidència", "Report Incident");
    }

    public static String selectOrderLabel(Context context) {
        return get(context, "Selecciona l'Ordre afectada", "Select the affected Order");
    }

    public static String incidentTypeLabel(Context context) {
        return get(context, "Tipus d'incidència", "Incident Type");
    }

    public static String priorityLabel(Context context) {
        return get(context, "Prioritat de la incidència", "Incident Priority");
    }

    public static String mozoGroupLabel(Context context) {
        return get(context, "Grup de Mozos Responsable", "Responsible Staff Group");
    }

    public static String descriptionLabel(Context context) {
        return get(context, "Descripció detallada", "Detailed Description");
    }

    public static String descriptionHint(Context context) {
        return get(context, "Explica què ha passat exactament...", "Explain exactly what happened...");
    }

    public static String sendReportButton(Context context) {
        return get(context, "ENVIAR REPORT A CENTRAL", "SEND REPORT TO CENTRAL");
    }

    public static String historyHeader(Context context) {
        return get(context, "Historial de Rutes", "Routes History");
    }

    public static String routesHeader(Context context) {
        return get(context, "Rutes Assignades", "Assigned Routes");
    }

    public static String pickingHeader(Context context) {
        return get(context, "Tasques de Picking", "Picking Tasks");
    }

    public static String sessionExpired(Context context) {
        return get(context, "Sessió caducada", "Session expired");
    }

    public static String errorDownloadingPalets(Context context) {
        return get(context, "Error al descarregar els palets", "Error downloading pallets");
    }

    public static String lotLabel(Context context) {
        return get(context, "Lot: ", "Lot: ");
    }

    public static String currentStatusLabel(Context context) {
        return get(context, "Estat actual: ", "Current status: ");
    }

    public static String profileHeader(Context context) {
        return get(context, "El Meu Perfil", "My Profile");
    }

    public static String profileLabelName(Context context) {
        return get(context, "Nom d'usuari", "Username");
    }

    public static String profileLabelRole(Context context) {
        return get(context, "Rol de l'usuari", "User Role");
    }

    public static String profileLabelPhone(Context context) {
        return get(context, "Telèfon", "Phone");
    }

    public static String profileLabelEmail(Context context) {
        return get(context, "Correu electrònic", "Email");
    }

    public static String profileErrorFetching(Context context) {
        return get(context, "Error al carregar el perfil", "Error loading profile");
    }

    public static String profileNotProvided(Context context) {
        return get(context, "No especificat", "Not specified");
    }

    public static String roleNotAllowed(Context context) {
        return get(context, "Error: L'aplicació mòbil només és per a Transportistes i Mossos.", "Error: Mobile app is restricted to Carriers and Warehouse Workers.");
    }
}
