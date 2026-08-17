import re

with open(r'c:\laragon\www\baingat-antigravy\APP-Ketinggian-Air\app\src\main\res\layout\fragment_pelaporan.xml', 'r', encoding='utf-8') as f:
    content = f.read()

def inject_preview(content, btn_id, preview_id, img_id, hapus_id):
    pattern = rf'(<LinearLayout\s+android:id="@+id/{btn_id}"[\s\S]*?</LinearLayout>)'
    
    preview_xml = f"""
                    <RelativeLayout
                        android:id="@+id/{preview_id}"
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:layout_marginTop="16dp"
                        android:visibility="gone">
                        
                        <com.google.android.material.imageview.ShapeableImageView
                            android:id="@+id/{img_id}"
                            android:layout_width="match_parent"
                            android:layout_height="200dp"
                            android:scaleType="centerCrop"
                            app:shapeAppearanceOverlay="@style/RoundedImageView"
                            android:background="#E5E7EB"/>

                        <ImageButton
                            android:id="@+id/{hapus_id}"
                            android:layout_width="36dp"
                            android:layout_height="36dp"
                            android:layout_alignParentEnd="true"
                            android:layout_margin="8dp"
                            android:src="@drawable/baseline_close_24"
                            android:background="@drawable/bg_close_btn"
                            app:tint="#FFFFFF"
                            android:padding="8dp"/>
                    </RelativeLayout>
"""
    return re.sub(pattern, r'\1' + preview_xml, content)

content = inject_preview(content, 'btnUploadFotoJalan', 'previewContainerJalan', 'imgPreviewJalan', 'btnHapusFotoJalan')
content = inject_preview(content, 'btnUploadFotoLokasi', 'previewContainerLokasi', 'imgPreviewLokasi', 'btnHapusFotoLokasi')
content = inject_preview(content, 'btnUploadFotoKendaraan', 'previewContainerKendaraan', 'imgPreviewKendaraan', 'btnHapusFotoKendaraan')

with open(r'c:\laragon\www\baingat-antigravy\APP-Ketinggian-Air\app\src\main\res\layout\fragment_pelaporan.xml', 'w', encoding='utf-8') as f:
    f.write(content)
print("done")
