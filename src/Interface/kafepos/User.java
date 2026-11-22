package com.kafepos;

/** Baris tabel users (kolom: id, kafe_id, username, password, nama, role, aktif). */
public class User {

    private int id;
    private int kafeId;
    private String nama;
    private String username;
    private String role = "kasir";
    private boolean aktif = true;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getKafeId() { return kafeId; }
    public void setKafeId(int kafeId) { this.kafeId = kafeId; }
    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public boolean isAktif() { return aktif; }
    public void setAktif(boolean aktif) { this.aktif = aktif; }
}
