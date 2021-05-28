using UnityEngine;

public class gun : MonoBehaviour
{
    public Camera playerCam;
    public GameObject player, nozzle, bulletPrefab;
    public player_controller playerScript;

    private bool canPlay;

    private void Start()
    {
        playerScript = player.GetComponent<player_controller>();
        canPlay = playerScript.canPlay;
    }

    // Update is called once per frame
    private void Update()
    {
        canPlay = playerScript.canPlay;
        if (Input.GetButtonDown("Fire1") && (playerScript.ammo > 0) && !GameObject.Find("bullet(Clone)") && canPlay == true)
        {
            Shoot();
            --playerScript.ammo;
        }
    }

    private void Shoot()
    {
        GameObject projectile = Instantiate(bulletPrefab, nozzle.transform.position, Quaternion.identity) as GameObject;
        bullet script = projectile.AddComponent<bullet>();
    }

}
