package unioeste.br.openvrt;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

public class SelectShapeActivity extends AppCompatActivity implements SelectShapeFragment.ShapeListFragmentInteractionListener {

    private void toMapsActivity(String mapLocation) {
        Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtra("map", mapLocation);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_shape);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, SelectShapeFragment.newInstance());
        fragmentTransaction.commit();
    }

    @Override
    public void onShapeListFragmentInteraction(String item) {
        toMapsActivity(item);
    }
}