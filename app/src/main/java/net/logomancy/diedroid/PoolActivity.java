//DieDroid -- An Android dice-roller app
//Copyright (C) 2010  Andrew Mike <logomancer@gmail.com>
//
//This program is free software: you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.
//
//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.
//
//For a copy of the GNU GPLv3, see http://www.gnu.org/licenses/gpl-3.0-standalone.html
//or http://www.gnu.org/licenses/translations.html for a translation into your local language.

package net.logomancy.diedroid;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class PoolActivity extends ActionBarActivity implements OnClickListener {
    DiceSpinListener misc = new DiceSpinListener(); // needed for implementation of dice spinner listener
    DieGroup pool = new DieGroup();
    Integer winValue = 0;
    Integer failValue = 0;
    Button winButton;
    Button failButton;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.optionmenu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menuAbout:
                Intent about = new Intent("org.openintents.action.SHOW_ABOUT_DIALOG");
                try {
                    startActivityForResult(about, 0);
                } catch (ActivityNotFoundException e) {
                    AlertDialog.Builder notFoundBuilder = new AlertDialog.Builder(this);
                    notFoundBuilder.setMessage(R.string.aboutNotFoundText)
                            .setTitle(R.string.aboutNotFoundTitle)
                            .setPositiveButton(R.string.commonYes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    try {
                                        Intent getApp = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.urlAboutMarket)));
                                        startActivity(getApp);
                                    } catch (ActivityNotFoundException e) {
                                        Intent getAppAlt = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.urlAboutWeb)));
                                        startActivity(getAppAlt);
                                    }
                                }
                            })
                            .setNegativeButton(R.string.commonNo, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                    AlertDialog notFound = notFoundBuilder.create();
                    notFound.show();
                }
                return true;
            case R.id.menuHelp:
                AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
                builder1.setMessage(R.string.menuHelpPool)
                        .setTitle(R.string.menuHelp)
                        .setNegativeButton(R.string.commonClose, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog help = builder1.create();
                help.show();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected Dialog onCreateDialog(int id) {
        Dialog popUp;
        switch (id) {
            case PoolDialog.POOL_DIALOG_WIN:
                popUp = new PoolDialog(this, id, new PoolDialog.ReturnListener() {
                    public void onReturn(int returned) {
                        String buttonText;
                        winValue = returned;
                        if (winValue > 0) {
                            StringBuilder sBuild = new StringBuilder();
                            sBuild.append(winValue);
                            sBuild.append(" ");
                            sBuild.append(getText(R.string.poolOrMore));
                            buttonText = sBuild.toString();
                        } else {
                            buttonText = "Disabled";
                        }
                        winButton.setText(buttonText);
                    }
                });
                break;
            case PoolDialog.POOL_DIALOG_FAIL:
                popUp = new PoolDialog(this, id, new PoolDialog.ReturnListener() {
                    public void onReturn(int returned) {
                        String buttonText;
                        failValue = returned;
                        if (failValue > 0) {
                            StringBuilder sBuild = new StringBuilder();
                            sBuild.append(failValue);
                            sBuild.append(" ");
                            sBuild.append(getText(R.string.poolOrLess));
                            buttonText = sBuild.toString();
                        } else {
                            buttonText = "Disabled";
                        }
                        failButton.setText(buttonText);
                    }
                });
                break;
            default:
                popUp = null;
        }
        return popUp;
    }

    protected void onPrepareDialog(int id, Dialog dialog) {
        PoolDialog pd = (PoolDialog) dialog;
        pd.setLimit(misc.getSelValue());
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case (R.id.poolRollButton):
                roll();
                break;
            case (R.id.poolWinButton):
                showDialog(PoolDialog.POOL_DIALOG_WIN);
                break;
            case (R.id.poolFailButton):
                showDialog(PoolDialog.POOL_DIALOG_FAIL);
                break;
        }
    }

    // click listener for roll button
    public void roll() {
        Integer numDice = -1;
        Integer numSides = -1;
        RollResult poolResult;

        // capture text boxes
        TextView tNumDice = (TextView) findViewById(R.id.poolDiceNum);
        TextView numWinsText = (TextView) findViewById(R.id.poolWin);
        TextView numFailsText = (TextView) findViewById(R.id.poolFail);

        // get values needed to roll dice
        numSides = misc.getSelValue();
        try { // catch blank strings in the text boxes
            numDice = Integer.valueOf(tNumDice.getText().toString());
        } catch (NumberFormatException e) {
            Toast.makeText(this, R.string.errorInvalidEntry, Toast.LENGTH_SHORT).show();
        }

        // check for error conditions
        if (numDice < 1) {
            // less than one die is rolled
            Toast.makeText(this, R.string.errorNotEnoughDice, Toast.LENGTH_SHORT).show();
        } else if ((winValue < 0) || (failValue < 0)) {
            // a success or failure value of less than 0 is not possible...
            Toast.makeText(this, R.string.errorWinFailNegative, Toast.LENGTH_SHORT).show();
        } else if ((winValue > numSides) || (failValue > numSides)) {
            // ...as is a success or failure value of greater than the number of sides on the dice
            Toast.makeText(this, R.string.errorWinFailOverflow, Toast.LENGTH_SHORT).show();
        } else if ((winValue > 0) && (winValue < failValue)) {
            // success and failure values are overlapping
            Toast.makeText(this, R.string.errorWinFailOverlap, Toast.LENGTH_SHORT).show();
        } else {
            pool.Quantity = numDice;
            pool.Sides = numSides;
            pool.winThreshold = winValue;
            pool.failThreshold = failValue;

            poolResult = pool.roll();

            // capture the GridView for the dice and attach an adapter based on the array of dice we just rolled
            GridView diceGrid = (GridView) findViewById(R.id.poolResultsGrid);
            diceGrid.setAdapter(new DiceAdapter(this, this, R.id.poolDieGridSquare, poolResult));

            // pop up a Toast stating the number of failures and successes
            numWinsText.setText(Integer.toString(poolResult.numWins));
            numFailsText.setText(Integer.toString(poolResult.numFails));
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pool); // set our layout

        // capture the spinner and attach a listener
        Spinner dieSpinner = (Spinner) findViewById(R.id.poolDiceSides);
        dieSpinner.setOnItemSelectedListener(misc);

        // ditto with the button
        Button roll = (Button) findViewById(R.id.poolRollButton);
        roll.setOnClickListener(this);

        winButton = (Button) findViewById(R.id.poolWinButton);
        winButton.setOnClickListener(this);

        failButton = (Button) findViewById(R.id.poolFailButton);
        failButton.setOnClickListener(this);
    }

}
